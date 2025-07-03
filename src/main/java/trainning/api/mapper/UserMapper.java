package trainning.api.mapper;

import org.springframework.stereotype.Component;
import trainning.api.dto.UserDto;
import trainning.api.model.UserModel;

@Component
public class UserMapper {
    public UserDto toDto(UserModel user) {
        return new UserDto(user.getId(), user.getUsername(), user.getRoles());
    }
}
