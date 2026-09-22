package backend.auth;

import backend.user.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,

            @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs) {
        byte[] secretBytes = Decoders.BASE64.decode(secret);

        this.secretKey = Keys.hmacShaKeyFor(secretBytes);

        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String generateAccessToken(AppUser user) {
        Instant now = Instant.now();

        Instant expiration = now.plusMillis(
                accessTokenExpirationMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("roleId", user.getRole().getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public String validateAndGetSubject(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }
}