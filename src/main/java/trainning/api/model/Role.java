package trainning.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    ADMIN("ADMIN"),
    USER_ADMIN("USER_ADMIN"),
    SIMPLE_USER("SIMPLE_USER");

    private final String name;
}
