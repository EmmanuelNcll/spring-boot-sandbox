package trainning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import trainning.api.model.RoleModel;

import java.util.Set;

@Getter
@Schema(description = "User response DTO")
public class UserDto {
    @Schema(description = "User ID")
    @NotNull(message = "User ID must not be null")
    private final Long id;
    @Schema(description = "Username")
    @NotNull(message = "Username must not be null")
    private final String username;
    private final Set<RoleDto> roles;

    public UserDto(Long id, String username, Set<RoleModel> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles.stream()
                .map(role -> new RoleDto(role.getName()))
                .collect(java.util.stream.Collectors.toSet());
    }
}
