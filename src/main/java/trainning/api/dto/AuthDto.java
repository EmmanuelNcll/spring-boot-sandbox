package trainning.api.dto;

public class AuthDto {
    private Long id;
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
