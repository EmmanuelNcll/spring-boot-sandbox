package trainning.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Assert;
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
import trainning.api.dto.CreateUserDto;
import trainning.api.model.UserModel;
import trainning.api.repository.RoleRepository;
import trainning.api.repository.UserRepository;
import trainning.api.service.AuthService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerV1Test {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private long simpleUserId;
    private long adminUserId;
    private String simpleUserToken;
    private String userAdminToken;
    private String adminToken;

    public static final String GET_USER_ENDPOINT = "/v1/user/";
    public static final String DELETE_USER_ENDPOINT = "/v1/user/";
    public static final String CREATE_USER_ENDPOINT = "/v1/user/create";
    public static final String PASSWORD = "1234";

    @BeforeEach
    public void setUpDatabase() {
        userRepository.deleteAll();

        simpleUserId = createUser("simpleUser", "SIMPLE_USER");
        simpleUserToken = authService.login(simpleUserId, PASSWORD);

        Long userAdminUserId = createUser("userAdminUser", "USER_ADMIN");
        userAdminToken = authService.login(userAdminUserId, PASSWORD);

        adminUserId = createUser("adminUser", "ADMIN");
        adminToken = authService.login(adminUserId, PASSWORD);
    }

    private long createUser(String username, String role) {
        UserModel simpleUser = new UserModel();
        simpleUser.setUsername(username);
        simpleUser.setPassword(passwordEncoder.encode(PASSWORD));
        simpleUser.setRole(roleRepository.findByName(role));
        return userRepository.save(simpleUser).getId();
    }

    @Test
    public void getUserSimpleUserForbidden() throws Exception {
        ResultActions resultActions = mockMvc.perform(get(GET_USER_ENDPOINT + simpleUserId)
                                                              .header("Authorization", "Bearer " + simpleUserToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void getUserUserAdminUserSuccess() throws Exception {
        ResultActions resultActions = mockMvc.perform(get(GET_USER_ENDPOINT + simpleUserId)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(simpleUserId))
                .andExpect(jsonPath("$.username").value("simpleUser"))
                .andExpect(jsonPath("$.roles[0].name").value("SIMPLE_USER"));
    }

    @Test
    public void getUserAdminUserSuccess() throws Exception {
        ResultActions resultActions = mockMvc.perform(get(GET_USER_ENDPOINT + simpleUserId)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(simpleUserId))
                .andExpect(jsonPath("$.username").value("simpleUser"))
                .andExpect(jsonPath("$.roles[0].name").value("SIMPLE_USER"));
    }

    @Test
    public void getUserWrongIdNotFound() throws Exception {
        long userId = 999L;

        ResultActions resultActions = mockMvc.perform(get(GET_USER_ENDPOINT + userId)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound()).andExpect(content().string("User with ID " + userId + " not found"));
    }

    @Test
    public void registerUserWithoutToken() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", "1234", Collections.singleton("SIMPLE_USER"));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void registerUserWithoutPermissionForbidden() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", "1234", Collections.singleton("SIMPLE_USER"));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + simpleUserToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void registerUserUsernameAlreadyExists() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("simpleUser", "1234", Collections.singleton("SIMPLE_USER"));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isConflict())
                .andExpect(content().string("Username already taken: simpleUser"));
    }

    @Test
    public void registerUserInvalidPayload() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content("{}"));

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void registerUserInvalidRole() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", "1234", Collections.singleton("INVALID_ROLE"));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string("Role does not exists: INVALID_ROLE"));
    }

    @Test
    public void registerUserAdminRole() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newAdmin", "1234", Collections.singleton("ADMIN"));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isForbidden())
                .andExpect(content().string("Cannot register user with ADMIN role"));
    }

    @Test
    public void registerUserWithoutRole() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", "1234", null);

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string("At least one role must be provided"));
    }

    @Test
    public void registerUserSuccessCreated() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", "1234", Collections.singleton("SIMPLE_USER"));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.roles[0].name").value("SIMPLE_USER"));

        JsonNode response = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString());
        Assert.isTrue(userRepository.existsById(response.get("id").asLong()));
    }

    @Test
    public void registerUserWithAdminSuccessCreated() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", "1234", Collections.singleton("SIMPLE_USER"));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.roles[0].name").value("SIMPLE_USER"));

        JsonNode response = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString());
        Assert.isTrue(userRepository.existsById(response.get("id").asLong()));
    }

    @Test
    public void registerUserMultipleRolesSuccessCreated() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add("SIMPLE_USER");
        roles.add("USER_ADMIN");
        CreateUserDto requestBody = new CreateUserDto("newUser", "1234", roles);

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.roles", hasSize(2)));

        JsonNode response = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString());
        Assert.isTrue(userRepository.existsById(response.get("id").asLong()));
    }

    @Test
    public void deleteUserWithoutTokenUnauthorized() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete(DELETE_USER_ENDPOINT + simpleUserId)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteUserWithoutPermissionForbidden() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete(DELETE_USER_ENDPOINT + simpleUserId)
                                                              .header("Authorization", "Bearer " + simpleUserToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void deleteUserWrongIdNotFound() throws Exception {
        long userId = 999L;

        ResultActions resultActions = mockMvc.perform(delete(DELETE_USER_ENDPOINT + userId)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + userId + " not found"));
    }

    @Test
    public void deleteUserWithAdminRoleForbidden() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete(DELETE_USER_ENDPOINT + adminUserId)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void deleteUserWithUserAdminSuccess() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete(DELETE_USER_ENDPOINT + simpleUserId)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(content().string(""));

        Assert.isTrue(!userRepository.existsById(simpleUserId));
    }


    @Test
    public void deleteUserWithAdminSuccess() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete(DELETE_USER_ENDPOINT + simpleUserId)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(content().string(""));

        Assert.isTrue(!userRepository.existsById(simpleUserId));
    }
}
