package trainning.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "api_role")
public class RoleModel {
    @Id
    @GeneratedValue
    private Long id;

    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    private String name;
}
