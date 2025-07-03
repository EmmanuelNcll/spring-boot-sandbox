package trainning.api.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "api_user")
public class UserModel {
    @Id
    @GeneratedValue
    private long id;
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "api_user_role",
            joinColumns = @JoinColumn(name = "api_user_id"),
            inverseJoinColumns = @JoinColumn(name = "api_role_id"))
    private Set<RoleModel> roles = new HashSet<>();

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<RoleModel> getRoles() {
        return roles;
    }

    public void setRole(RoleModel role) {
        this.roles.add(role);
    }
}
