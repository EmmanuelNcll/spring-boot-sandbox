package trainning.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "api_user")
public class UserModel {
    @Id
    @GeneratedValue
    private long id;
    @Setter
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Setter
    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "api_user_role",
            joinColumns = @JoinColumn(name = "api_user_id"),
            inverseJoinColumns = @JoinColumn(name = "api_role_id"))
    private Set<RoleModel> roles;

    public void addRole(RoleModel role) {
        if( this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    public void removeRoles() {
        if (this.roles != null) {
            this.roles.clear();
        }
    }
}
