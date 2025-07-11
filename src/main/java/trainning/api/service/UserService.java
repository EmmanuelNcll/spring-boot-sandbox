package trainning.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import trainning.api.exception.*;
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

        List<RoleModel> allRoles = (List<RoleModel>) roleRepository.findAll();
        if (roles == null || roles.isEmpty()) {
            throw new InvalidRoleException("At least one role must be provided");
        }
        List<RoleModel> userRoles = roles.stream()
                .map(roleName -> allRoles.stream()
                        .filter(role -> role.getName().equals(roleName))
                        .findFirst()
                        .orElseThrow(() -> new InvalidRoleException("Role does not exists: " + roleName))).toList();

        userRoles.stream()
                .filter(role -> role.getName().equals("ADMIN"))
                .findAny()
                .ifPresent(role -> {
                    throw new AdminRoleException("Cannot register user with ADMIN role");
                });

        UserModel user = new UserModel();
        user.setUsername(username);

        validatePassword(rawPassword);
        user.setPassword(passwordEncoder.encode(rawPassword));

        for(RoleModel role : userRoles) {
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    public UserModel getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }

    public String deleteUser(long id) {
        UserModel user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            throw new AdminRoleException("Cannot delete user with ADMIN role");
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

        if (!rolesFromToken.contains("ADMIN") && !rolesFromToken.contains("USER_ADMIN")) {
            if (idFromToken != id) {
                throw new UserNotAllowedException("User with ID " + idFromToken + " is only allowed to modify his own password, not the one of user with ID " + id);
            }
        }

        if (userToRename.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN") || role.getName().equals("USER_ADMIN"))) {
            if (idFromToken != id) {
                throw new UserNotAllowedException("It is not allowed to modify the password of a user with ADMIN or USER_ADMIN role, unless you are the user itself");
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
}
