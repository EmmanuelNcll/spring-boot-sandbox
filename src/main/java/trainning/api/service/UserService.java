package trainning.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import trainning.api.exception.InvalidRoleException;
import trainning.api.exception.UserAlreadyExistsException;
import trainning.api.exception.UserNotFoundException;
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
        List<RoleModel> userRoles = roles.stream()
                .map(roleName -> allRoles.stream()
                        .filter(role -> role.getName().equals(roleName))
                        .findFirst()
                        .orElseThrow(() -> new InvalidRoleException("Role does not exists: " + roleName))).toList();

        // TODO: add rules for password strength

        UserModel user = new UserModel();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        for(RoleModel role : userRoles) {
            if (role.getName().equals("ADMIN")) {
                throw new InvalidRoleException("Cannot register user with ADMIN role");
            }
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    public UserModel getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }
}