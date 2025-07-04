package trainning.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trainning.api.dto.AuthDto;
import trainning.api.service.AuthService;

@RestController
@RequestMapping("/v1")
public class AuthControllerV1 {
    @Autowired
    private AuthService authService;

    @PostMapping("/auth")
    public ResponseEntity<String> auth(@RequestBody AuthDto authRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(
                authService.login(authRequest.getId(), authRequest.getPassword())
        );
    }
}
