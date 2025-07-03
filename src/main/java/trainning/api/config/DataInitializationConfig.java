package trainning.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import trainning.api.model.RoleModel;
import trainning.api.model.UserModel;
import trainning.api.repository.RoleRepository;
import org.springframework.transaction.annotation.Transactional;
import trainning.api.repository.UserRepository;

@Configuration
public class DataInitializationConfig {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    ApplicationRunner initDatabase() {
        return args -> { // If database is empty
            if (roleRepository.count() == 0) { // Create roles
                RoleModel adminRole = new RoleModel();
                adminRole.setName("ADMIN");
                roleRepository.save(adminRole);

                RoleModel userAdminRole = new RoleModel();
                userAdminRole.setName("USER_ADMIN");
                roleRepository.save(userAdminRole);

                RoleModel simpleUserRole = new RoleModel();
                simpleUserRole.setName("SIMPLE_USER");
                roleRepository.save(simpleUserRole);
            }
            if (userRepository.count() == 0) { // Create admin user
                UserModel adminUser = new UserModel();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("ap1_ADmiN_p@55w0rD"));
                adminUser.setRole(
                        roleRepository.findByName("ADMIN")
                );

                userRepository.save(adminUser);
            }
        };
    }
}