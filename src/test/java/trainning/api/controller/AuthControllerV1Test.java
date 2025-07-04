package trainning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import trainning.api.dto.AuthDto;
import trainning.api.exception.InvalidCredentialsException;
import trainning.api.exception.UserNotFoundException;
import trainning.api.model.UserModel;
import trainning.api.repository.RoleRepository;
import trainning.api.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerV1Test {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private long simpleUserId;
    private final String simpleUser = "simpleUser";
    private final String password = "1234";
    private final String authEndpoint = "/v1/auth";

    @BeforeEach
    public void setUpDatabase() {
        userRepository.deleteAll();

        UserModel simpleUser = new UserModel();
        simpleUser.setUsername(this.simpleUser);
        simpleUser.setPassword(passwordEncoder.encode(password));
        simpleUser.setRole(roleRepository.findByName("SIMPLE_USER"));
        simpleUserId = userRepository.save(simpleUser).getId();
    }

    @Test
    public void wrongPayload() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                post(authEndpoint)
                        .content("")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void invalidUser() throws Exception {
        AuthDto authDto = new AuthDto(999L, password);

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(
                post(authEndpoint)
                        .content(objectMapper.writeValueAsString(authDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        resultActions.andExpect(status().isNotFound())
                .andExpect(result -> {
                    Throwable exception = result.getResolvedException();
                    if (exception instanceof UserNotFoundException) {
                        // Expected exception
                    } else {
                        throw new RuntimeException("Expected UserNotFoundException, but got: " + exception);
                    }
                });
    }

    @Test
    public void invalidPassword() throws Exception {
        AuthDto authDto = new AuthDto(simpleUserId, "wrong_password");

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(
                post(authEndpoint)
                        .content(objectMapper.writeValueAsString(authDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        resultActions.andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Throwable exception = result.getResolvedException();
                    if (exception instanceof InvalidCredentialsException) {
                        // Expected exception
                    } else {
                        throw new RuntimeException("Expected InvalidCredentialsException, but got: " + exception);
                    }
                });
    }

    @Test
    public void wrongMethod() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get(authEndpoint)
                        .accept(MediaType.APPLICATION_JSON)
        );

        resultActions.andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void validAuthentication() throws Exception {
        AuthDto authDto = new AuthDto(simpleUserId, password);

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(
                post(authEndpoint)
                        .content(objectMapper.writeValueAsString(authDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        resultActions.andExpect(status().isOk());
        assertTrue(resultActions.andReturn().getResponse().getContentAsString().matches(".*\\..*\\..*"));
    }

    // TODO: Add tests for expired token
}
