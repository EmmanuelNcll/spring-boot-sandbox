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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import trainning.api.dto.AuthDto;
import trainning.api.service.AuthService;

@RestController
@RequestMapping("/v1")
@Validated
@Tag(name = "Authentication", description = "Endpoint for authentication")
public class AuthControllerV1 {
    @Autowired
    private AuthService authService;

    @Operation(summary = "Authenticate user", description = "Authenticates the user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful authentication"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload / Invalid password", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/auth")
    public ResponseEntity<String> auth(@RequestBody @Valid AuthDto authRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(
                authService.login(authRequest.getId(), authRequest.getPassword())
        );
    }
}
