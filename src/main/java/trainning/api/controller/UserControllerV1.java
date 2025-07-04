package trainning.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import trainning.api.dto.CreateUserDto;
import trainning.api.dto.UserDto;
import trainning.api.mapper.UserMapper;
import trainning.api.service.UserService;

@RestController
@RequestMapping("/v1")
public class UserControllerV1 {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER_ADMIN')")
    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable long id) {
        return ResponseEntity.status(HttpStatus.OK).body(
                userMapper.toDto(userService.getUser(id))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER_ADMIN')")
    @PostMapping("/user/create")
    public ResponseEntity<UserDto> registerUser(@RequestBody CreateUserDto userDto) {
        UserDto createdUser = userMapper.toDto(
                userService.registerUser(userDto.getUsername(), userDto.getPassword(), userDto.getRoles())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // TODO: add password modification endpoint?

    // TODO: add role modification endpoint?

    // TODO: add endpoint to delete users?
}