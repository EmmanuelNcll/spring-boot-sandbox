package trainning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = "Authentication request DTO")
public class AuthDto {
    @Schema(description = "User ID")
    @NotNull(message = "User ID must not be null")
    private Long id;
    @Schema(description = "User password")
    @NotNull(message = "Password must not be null")
    private String password;
}
