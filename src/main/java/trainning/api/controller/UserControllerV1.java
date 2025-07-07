package trainning.api.controller;

import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/v1")
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
            @ApiResponse(responseCode = "403", description = "User not authorized to do this operation"),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_ADMIN')")
    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable long id) {
        return ResponseEntity.status(HttpStatus.OK).body(
                userMapper.toDto(userService.getUser(id))
        );
    }

    @Operation(summary = "Create a new user", description = "Registers a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload / Role not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to do this operation"),
            @ApiResponse(responseCode = "409", description = "Username already exists"),
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_ADMIN')")
    @PostMapping("/user/create")
    public ResponseEntity<UserDto> registerUser(@RequestBody @Valid CreateUserDto userDto) {
        UserDto createdUser = userMapper.toDto(
                userService.registerUser(userDto.getUsername(), userDto.getPassword(), userDto.getRoles())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // TODO: add password modification endpoint?

    // TODO: add role modification endpoint?

    // TODO: add endpoint to delete users?
}