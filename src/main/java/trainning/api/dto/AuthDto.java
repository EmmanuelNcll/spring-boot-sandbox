package trainning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Authentication request DTO")
public class AuthDto {
    @Schema(description = "User ID")
    @NotNull(message = "User ID must not be null")
    private Long id;
    @Schema(description = "User password")
    @NotNull(message = "Password must not be null")
    private String password;

    public AuthDto(Long id, String password) {
        this.id = id;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public Long getId() {
        return id;
    }
}
