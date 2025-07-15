package trainning.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import trainning.api.exception.*;
import trainning.api.model.Role;
import trainning.api.model.RoleModel;
import trainning.api.model.UserModel;
import trainning.api.repository.RoleRepository;
import trainning.api.repository.UserRepository;

import java.util.List;
import java.util.Set;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserModel registerUser(String username, String rawPassword, Set<String> roles) {
        if (userRepository.findByUsername(username) != null) {
            throw new UserAlreadyExistsException("Username already taken: " + username);
        }

        if (roles == null || roles.isEmpty()) {
            throw new InvalidRoleException("At least one role must be provided");
        }

        if (roles.contains(Role.ADMIN.getName())) {
            throw new AdminRoleException("Cannot register user with " + Role.ADMIN.getName() + " role");
        }

        List<RoleModel> userRoles = convertToRoleModel(roles);

        UserModel user = new UserModel();
        user.setUsername(username);

        validatePassword(rawPassword);
        user.setPassword(passwordEncoder.encode(rawPassword));

        for(RoleModel role : userRoles) {
            user.addRole(role);
        }

        return userRepository.save(user);
    }

    public UserModel getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }

    public String deleteUser(long id) {
        UserModel user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals(Role.ADMIN.getName()))) {
            throw new AdminRoleException("Cannot delete user with " + Role.ADMIN.getName() + " role");
        }

        userRepository.delete(user);

        return "";
    }

    public UserModel modifyPassword(long id, String newPassword) {
        UserModel userToRename = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        long idFromToken = Long.parseLong(authentication.getPrincipal().toString());
        List<String> rolesFromToken = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();

        if (!rolesFromToken.contains(Role.ADMIN.getName()) && !rolesFromToken.contains(Role.USER_ADMIN.getName())) {
            if (idFromToken != id) {
                throw new UserNotAllowedException("User with ID " + idFromToken + " is only allowed to modify his own password, not the one of user with ID " + id);
            }
        }

        if (userToRename.getRoles().stream().anyMatch(role -> role.getName().equals(Role.ADMIN.getName()) || role.getName().equals(Role.USER_ADMIN.getName()))) {
            if (idFromToken != id) {
                throw new UserNotAllowedException("It is not allowed to modify the password of a user with " + Role.ADMIN.getName() + " or " + Role.USER_ADMIN.getName() + " role, unless you are the user itself");
            }
        }

        validatePassword(newPassword);

        userToRename.setPassword(passwordEncoder.encode(newPassword));

        return userRepository.save(userToRename);
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new InvalidPasswordException("Password must not be null or empty");
        }
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+=-]).{10,}$";

        if (!password.matches(passwordPattern)) {
            throw new InvalidPasswordException("Password must be at least 10 characters long, contain at least one digit, one lowercase letter, one uppercase letter, and one special character");
        }
    }

    public UserModel modifyRole(long id, Set<String> roles) {
        UserModel userToModify = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        if (userToModify.getRoles().stream().anyMatch(role -> role.getName().equals(Role.ADMIN.getName()))) {
            throw new AdminRoleException("Cannot modify roles of user with " + Role.ADMIN.getName() + " role");
        }

        if (roles == null || roles.isEmpty()) {
            throw new InvalidRoleException("At least one role must be provided");
        }

        if (roles.contains(Role.ADMIN.getName())) {
            throw new AdminRoleException("Cannot give role " + Role.ADMIN.getName() + " to user");
        }

        List<RoleModel> newRoles = convertToRoleModel(roles);

        userToModify.removeRoles();
        for (RoleModel role : newRoles) {
            userToModify.addRole(role);
        }

        return userRepository.save(userToModify);
    }

    private List<RoleModel> convertToRoleModel(Set<String> rolesToCheck) {
        List<RoleModel> allRoles = (List<RoleModel>) roleRepository.findAll();

        return rolesToCheck.stream()
                .map(roleName -> allRoles.stream()
                .filter(role -> role.getName().equals(roleName))
                .findFirst()
                .orElseThrow(() -> new InvalidRoleException("Role does not exists: " + roleName))).toList();
    }
}
