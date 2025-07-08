package trainning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
@Schema(description = "User Creation DTO")
public class CreateUserDto {
    @Schema(description = "Username")
    @NotNull(message = "Username must not be null")
    private String username;
    @Schema(description = "User password")
    @NotNull(message = "Password must not be null")
    private String password;
    @Schema(description = "User roles")
    private Set<String> roles;
}