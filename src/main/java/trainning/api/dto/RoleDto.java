package trainning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Role DTO")
public class RoleDto {
    @Schema(description = "Role name")
    @NotNull(message = "Role must not be null")
    private String name;

    public RoleDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
