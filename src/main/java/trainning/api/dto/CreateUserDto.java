package trainning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

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

    public CreateUserDto(String username, String password, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<String> getRoles() {
        return roles;
    }
}