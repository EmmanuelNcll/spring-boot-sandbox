package trainning.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import trainning.api.dto.CreateUserDto;
import trainning.api.dto.UserDto;
import trainning.api.mapper.UserMapper;
import trainning.api.service.UserService;

import java.util.Set;

@RestController
@RequestMapping("/v1/user")
@Validated
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserControllerV1 {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT Token is missing or invalid", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "User not authorized to do this operation", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable @Valid long id) {
        return ResponseEntity.status(HttpStatus.OK).body(
                userMapper.toDto(userService.getUser(id))
        );
    }

    @Operation(summary = "Delete a user", description = "Deletes a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT Token is missing or invalid", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "User not authorized to do this operation", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable @Valid long id) {
        return ResponseEntity.status(HttpStatus.OK).body(
                userService.deleteUser(id)
        );
    }

    @Operation(summary = "Modify user's password", description = "Modify the password of the user (can be done by the user itself)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password modified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload / Invalid password", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT Token is missing or invalid", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "User not authorized to do this operation", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "405", description = "Wrong Method", content = @Content(mediaType = "application/json")),
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_ADMIN', 'SIMPLE_USER')")
    @PostMapping("/{id}/password")
    public ResponseEntity<UserDto> modifyPassword(@PathVariable @Valid long id, @RequestBody @Valid String newPassword) {
        UserDto modifiedUser = userMapper.toDto(
                userService.modifyPassword(id, newPassword)
        );
        return ResponseEntity.status(HttpStatus.OK).body(modifiedUser);
    }

    @Operation(summary = "Modify user roles", description = "Replacing the roles of an existing user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User roles modified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload / Role not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT Token is missing or invalid", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "User not authorized to do this operation / Cannot give user role ADMIN / Cannot modify ADMIN roles", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("{id}/modify")
    public ResponseEntity<UserDto> modifyRole(@PathVariable @Valid long id, @RequestBody @Valid Set<String> roles) {
        UserDto modifiedUser = userMapper.toDto(
                userService.modifyRole(id, roles)
        );
        return ResponseEntity.status(HttpStatus.OK).body(modifiedUser);
    }

    @Operation(summary = "Create a new user", description = "Registers a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload / Role not found / Invalid password", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT Token is missing or invalid", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "User not authorized to do this operation / Cannot register user with ADMIN role", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Username already exists", content = @Content(mediaType = "application/json")),
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<UserDto> registerUser(@RequestBody @Valid CreateUserDto userDto) {
        UserDto createdUser = userMapper.toDto(
                userService.registerUser(userDto.getUsername(), userDto.getPassword(), userDto.getRoles())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
