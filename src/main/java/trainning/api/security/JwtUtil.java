package trainning.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import trainning.api.model.RoleModel;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;
    private SecretKey key;
    
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String generateToken(Long id, Set<RoleModel> roles) {
        return Jwts.builder()
                .subject(id.toString())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .claim("roles", roles.stream().map(RoleModel::getName).toList())
                .signWith(key)
                .compact();
    }
    
    public Long getIdFromToken(String token) {
        String id = Jwts.parser()
                .verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(id);
    }

    public List<String> getRolesFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("roles", List.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            // TODO: verify that user stills exists with same roles?
            return true;
        } catch (SecurityException e) { // TODO: work on better error handling
            System.out.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
}