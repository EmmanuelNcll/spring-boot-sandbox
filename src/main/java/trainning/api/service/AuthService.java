package trainning.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import trainning.api.exception.InvalidCredentialsException;
import trainning.api.exception.UserNotFoundException;
import trainning.api.model.UserModel;
import trainning.api.repository.UserRepository;
import trainning.api.security.JwtUtil;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    public String login(Long id, String rawPassword) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return jwtUtil.generateToken(id, user.getRoles());
        } else {
            throw new InvalidCredentialsException("Invalid password for user " + id);
        }
    }
}
