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
import trainning.api.model.Role;
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
    private long userAdminId;
    private long adminId;
    private String simpleUserToken;
    private String userAdminToken;
    private String adminToken;

    public static final String API_PREFIX = "/v1/";
    public static final String GET_USER_ENDPOINT = API_PREFIX + "user/";
    public static final String DELETE_USER_ENDPOINT = API_PREFIX + "user/";
    public static final String CREATE_USER_ENDPOINT = API_PREFIX + "user/create";
    public static final String MODIFY_PASSWORD_PREFIX = API_PREFIX + "user/";
    public static final String MODIFY_PASSWORD_SUFFIX = "/password";
    public static final String MODIFY_ROLE_PREFIX = API_PREFIX + "user/";
    public static final String MODIFY_ROLE_SUFFIX = "/modify";

    public static final String SIMPLE_USER_USERNAME = "simpleUser";
    public static final String USER_ADMIN_USERNAME = "userAdminUser";
    public static final String ADMIN_USERNAME = "adminUser";
    public static final String PASSWORD = "Password_1234";
    public static final String INVALID_PASSWORD = "1234";

    @BeforeEach
    public void setUpDatabase() {
        userRepository.deleteAll();

        simpleUserId = createUser(SIMPLE_USER_USERNAME, Role.SIMPLE_USER.getName());
        simpleUserToken = authService.login(simpleUserId, PASSWORD);

        userAdminId = createUser(USER_ADMIN_USERNAME, Role.USER_ADMIN.getName());
        userAdminToken = authService.login(userAdminId, PASSWORD);

        adminId = createUser(ADMIN_USERNAME, Role.ADMIN.getName());
        adminToken = authService.login(adminId, PASSWORD);
    }

    private long createUser(String username, String role) {
        UserModel simpleUser = new UserModel();
        simpleUser.setUsername(username);
        simpleUser.setPassword(passwordEncoder.encode(PASSWORD));
        simpleUser.addRole(roleRepository.findByName(role));
        return userRepository.save(simpleUser).getId();
    }

    @Test
    public void getUserSimpleUserForbidden() throws Exception {
        ResultActions resultActions = mockMvc.perform(get(GET_USER_ENDPOINT + simpleUserId)
                                                              .header("Authorization", "Bearer " + simpleUserToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    public void getUserUserAdminUserSuccess() throws Exception {
        ResultActions resultActions = mockMvc.perform(get(GET_USER_ENDPOINT + simpleUserId)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(simpleUserId))
                .andExpect(jsonPath("$.username").value(SIMPLE_USER_USERNAME))
                .andExpect(jsonPath("$.roles[0].name").value(Role.SIMPLE_USER.getName()));
    }

    @Test
    public void getUserAdminUserSuccess() throws Exception {
        ResultActions resultActions = mockMvc.perform(get(GET_USER_ENDPOINT + simpleUserId)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(simpleUserId))
                .andExpect(jsonPath("$.username").value(SIMPLE_USER_USERNAME))
                .andExpect(jsonPath("$.roles[0].name").value(Role.SIMPLE_USER.getName()));
    }

    @Test
    public void getUserWrongId() throws Exception {
        long userId = 999L;

        ResultActions resultActions = mockMvc.perform(get(GET_USER_ENDPOINT + userId)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + userId + " not found"));
    }

    @Test
    public void getUserWrongIdType() throws Exception {
        String userId = "abc";

        ResultActions resultActions = mockMvc.perform(get(GET_USER_ENDPOINT + userId)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void registerUserWithoutToken() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", PASSWORD, Collections.singleton(Role.SIMPLE_USER.getName()));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    public void registerUserWithoutPermission() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", PASSWORD, Collections.singleton(Role.SIMPLE_USER.getName()));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + simpleUserToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    public void registerUserUsernameAlreadyExists() throws Exception {
        CreateUserDto requestBody = new CreateUserDto(SIMPLE_USER_USERNAME, PASSWORD, Collections.singleton(Role.SIMPLE_USER.getName()));

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

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void registerUserInvalidRole() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", PASSWORD, Collections.singleton("INVALID_ROLE"));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string("Role does not exists: INVALID_ROLE"));
    }

    @Test
    public void registerUserInvalidPassword() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", INVALID_PASSWORD, Collections.singleton(Role.SIMPLE_USER.getName()));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string("Password must be at least 10 characters long, contain at least one digit, one lowercase letter, one uppercase letter, and one special character"));
    }

    @Test
    public void registerUserAdminRole() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newAdmin", PASSWORD, Collections.singleton(Role.ADMIN.getName()));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isForbidden())
                .andExpect(content().string("Cannot register user with " + Role.ADMIN.getName() + " role"));
    }

    @Test
    public void registerUserWithoutRole() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", PASSWORD, null);

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
        CreateUserDto requestBody = new CreateUserDto("newUser", PASSWORD, Collections.singleton(Role.SIMPLE_USER.getName()));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.roles[0].name").value(Role.SIMPLE_USER.getName()));

        JsonNode response = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString());
        Assert.isTrue(userRepository.existsById(response.get("id").asLong()));
    }

    @Test
    public void registerUserWithAdminSuccessCreated() throws Exception {
        CreateUserDto requestBody = new CreateUserDto("newUser", PASSWORD, Collections.singleton(Role.SIMPLE_USER.getName()));

        ObjectMapper objectMapper = new ObjectMapper();
        ResultActions resultActions = mockMvc.perform(post(CREATE_USER_ENDPOINT)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(requestBody)));

        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.roles[0].name").value(Role.SIMPLE_USER.getName()));

        JsonNode response = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString());
        Assert.isTrue(userRepository.existsById(response.get("id").asLong()));
    }

    @Test
    public void registerUserMultipleRolesSuccessCreated() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add(Role.SIMPLE_USER.getName());
        roles.add(Role.USER_ADMIN.getName());
        CreateUserDto requestBody = new CreateUserDto("newUser", PASSWORD, roles);

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
    public void deleteUserWithoutToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete(DELETE_USER_ENDPOINT + simpleUserId)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    public void deleteUserWithoutPermission() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete(DELETE_USER_ENDPOINT + simpleUserId)
                                                              .header("Authorization", "Bearer " + simpleUserToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    public void deleteUserWrongId() throws Exception {
        long userId = 999L;

        ResultActions resultActions = mockMvc.perform(delete(DELETE_USER_ENDPOINT + userId)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + userId + " not found"));
    }

    @Test
    public void deleteUserWrongIdType() throws Exception {
        String userId = "abc";

        ResultActions resultActions = mockMvc.perform(delete(DELETE_USER_ENDPOINT + userId)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void deleteUserWithAdminRole() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete(DELETE_USER_ENDPOINT + adminId)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden())
                .andExpect(content().string("Cannot delete user with " + Role.ADMIN.getName() + " role"));
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

    @Test
    public void modifyPasswordInvalidPayload() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(MODIFY_PASSWORD_PREFIX + simpleUserId + MODIFY_PASSWORD_SUFFIX)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(""));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void modifyPasswordInvalidPassword() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(MODIFY_PASSWORD_PREFIX + simpleUserId + MODIFY_PASSWORD_SUFFIX)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(INVALID_PASSWORD));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string("Password must be at least 10 characters long, contain at least one digit, one lowercase letter, one uppercase letter, and one special character"));
    }

    @Test
    public void modifyPasswordWithoutToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(MODIFY_PASSWORD_PREFIX + simpleUserId + MODIFY_PASSWORD_SUFFIX)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(PASSWORD));

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    public void modifyPasswordWithoutPermission() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(MODIFY_PASSWORD_PREFIX + adminId + MODIFY_PASSWORD_SUFFIX)
                                                              .header("Authorization", "Bearer " + simpleUserToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(PASSWORD));

        resultActions.andExpect(status().isForbidden())
                .andExpect(content().string("User with ID " + simpleUserId + " is only allowed to modify his own password, not the one of user with ID " + adminId));
    }

    @Test
    public void modifyPasswordWrongId() throws Exception {
        long userId = 999L;

        ResultActions resultActions = mockMvc.perform(post(MODIFY_PASSWORD_PREFIX + userId + MODIFY_PASSWORD_SUFFIX)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(PASSWORD));

        resultActions.andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + userId + " not found"));
    }

    @Test
    public void modifyPasswordWrongIdType() throws Exception {
        long userId = 999L;

        ResultActions resultActions = mockMvc.perform(post(MODIFY_PASSWORD_PREFIX + userId + MODIFY_PASSWORD_SUFFIX)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(PASSWORD));

        resultActions.andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + userId + " not found"));
    }

    @Test
    public void modifyUserAdminPasswordByAdmin() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(MODIFY_PASSWORD_PREFIX + userAdminId + MODIFY_PASSWORD_SUFFIX)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(PASSWORD));

        resultActions.andExpect(status().isForbidden())
                .andExpect(content().string("It is not allowed to modify the password of a user with " + Role.ADMIN.getName() + " or " + Role.USER_ADMIN.getName() + " role, unless you are the user itself"));
    }

    @Test
    public void modifyPasswordByUserSuccess() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(MODIFY_PASSWORD_PREFIX + simpleUserId + MODIFY_PASSWORD_SUFFIX)
                                                              .header("Authorization", "Bearer " + simpleUserToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(PASSWORD));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(simpleUserId))
                .andExpect(jsonPath("$.username").value(SIMPLE_USER_USERNAME));
    }

    @Test
    public void modifyPasswordByUserAdminSuccess() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(MODIFY_PASSWORD_PREFIX + userAdminId + MODIFY_PASSWORD_SUFFIX)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(PASSWORD));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userAdminId))
                .andExpect(jsonPath("$.username").value(USER_ADMIN_USERNAME));
    }

    @Test
    public void modifyPasswordByAdminSuccess() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(MODIFY_PASSWORD_PREFIX + adminId + MODIFY_PASSWORD_SUFFIX)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(PASSWORD));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(adminId))
                .andExpect(jsonPath("$.username").value(ADMIN_USERNAME));
    }

    @Test
    public void modifyRoleWithoutToken() throws Exception {
        Set<String> roles = Collections.singleton(Role.SIMPLE_USER.getName());
        ObjectMapper objectMapper = new ObjectMapper();

        ResultActions resultActions = mockMvc.perform(post(MODIFY_ROLE_PREFIX + simpleUserId + MODIFY_ROLE_SUFFIX)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(roles)));

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    public void modifyRoleWithoutAdminPermission() throws Exception {
        Set<String> roles = Collections.singleton(Role.SIMPLE_USER.getName());
        ObjectMapper objectMapper = new ObjectMapper();

        ResultActions resultActions = mockMvc.perform(post(MODIFY_ROLE_PREFIX + simpleUserId + MODIFY_ROLE_SUFFIX)
                                                              .header("Authorization", "Bearer " + userAdminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(roles)));

        resultActions.andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    public void modifyRoleWrongId() throws Exception {
        long userId = 999L;
        Set<String> roles = Collections.singleton(Role.ADMIN.getName());
        ObjectMapper objectMapper = new ObjectMapper();

        ResultActions resultActions = mockMvc.perform(post(MODIFY_ROLE_PREFIX + userId + MODIFY_ROLE_SUFFIX)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(roles)));

        resultActions.andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + userId + " not found"));
    }

    @Test
    public void modifyRoleWrongIdType() throws Exception {
        String userId = "abc";
        Set<String> roles = Collections.singleton(Role.ADMIN.getName());
        ObjectMapper objectMapper = new ObjectMapper();

        ResultActions resultActions = mockMvc.perform(post(MODIFY_ROLE_PREFIX + userId + MODIFY_ROLE_SUFFIX)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(roles)));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void modifyRoleWithAdminRole() throws Exception {
        Set<String> roles = Collections.singleton(Role.ADMIN.getName());
        ObjectMapper objectMapper = new ObjectMapper();

        ResultActions resultActions = mockMvc.perform(post(MODIFY_ROLE_PREFIX + simpleUserId + MODIFY_ROLE_SUFFIX)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(roles)));

        resultActions.andExpect(status().isForbidden())
                .andExpect(content().string("Cannot give role " + Role.ADMIN.getName() + " to user"));
    }

    @Test
    public void modifyRoleOfAdminUser() throws Exception {
        Set<String> roles = Collections.singleton(Role.SIMPLE_USER.getName());
        ObjectMapper objectMapper = new ObjectMapper();

        ResultActions resultActions = mockMvc.perform(post(MODIFY_ROLE_PREFIX + adminId + MODIFY_ROLE_SUFFIX)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(roles)));

        resultActions.andExpect(status().isForbidden())
                .andExpect(content().string("Cannot modify roles of user with " + Role.ADMIN.getName() + " role"));
    }

    @Test
    public void modifyRoleInvalidRole() throws Exception {
        Set<String> roles = Collections.singleton("INVALID_ROLE");
        ObjectMapper objectMapper = new ObjectMapper();

        ResultActions resultActions = mockMvc.perform(post(MODIFY_ROLE_PREFIX + simpleUserId + MODIFY_ROLE_SUFFIX)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(roles)));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string("Role does not exists: INVALID_ROLE"));
    }

    @Test
    public void modifyRoleInvalidPayload() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(MODIFY_ROLE_PREFIX + simpleUserId + MODIFY_ROLE_SUFFIX)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(""));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void modifyRoleSuccess() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add(Role.SIMPLE_USER.getName());
        roles.add(Role.USER_ADMIN.getName());
        ObjectMapper objectMapper = new ObjectMapper();

        ResultActions resultActions = mockMvc.perform(post(MODIFY_ROLE_PREFIX + simpleUserId + MODIFY_ROLE_SUFFIX)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(roles)));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(simpleUserId))
                .andExpect(jsonPath("$.roles", hasSize(2)));
    }

    @Test
    public void modifyRoleSameRoleTwiceSuccess() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add(Role.SIMPLE_USER.getName());
        roles.add(Role.SIMPLE_USER.getName());
        ObjectMapper objectMapper = new ObjectMapper();

        ResultActions resultActions = mockMvc.perform(post(MODIFY_ROLE_PREFIX + simpleUserId + MODIFY_ROLE_SUFFIX)
                                                              .header("Authorization", "Bearer " + adminToken)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(roles)));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(simpleUserId))
                .andExpect(jsonPath("$.roles", hasSize(1)));
    }
}
