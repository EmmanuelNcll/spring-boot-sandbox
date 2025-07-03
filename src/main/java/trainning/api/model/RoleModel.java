package trainning.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "api_role")
public class RoleModel {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
