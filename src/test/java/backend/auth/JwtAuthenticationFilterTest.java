package backend.auth;

import backend.user.AppUser;
import backend.user.AppUserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private final byte[] key = new byte[32];
    private final JwtService jwt = new JwtService(Encoders.BASE64.encode(key), 60000);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, users);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private String token(UUID id, Instant expiration, byte[] signingKey) {
        return Jwts.builder().subject(id.toString()).expiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(signingKey)).compact();
    }

    private void request(String token) throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});
    }

    @Test
    void authenticatesActiveUserByUuid() throws Exception {
        UUID id = UUID.randomUUID();
        var user = new AppUser();
        user.setId(id);
        when(users.findById(id)).thenReturn(Optional.of(user));
        request(token(id, Instant.now().plusSeconds(60), key));
        assertEquals(id.toString(), SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        request(token(UUID.randomUUID(), Instant.now().minusSeconds(60), key));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(users);
    }

    @Test
    void rejectsWrongSignature() throws Exception {
        byte[] otherKey = new byte[32];
        otherKey[0] = 1;
        request(token(UUID.randomUUID(), Instant.now().plusSeconds(60), otherKey));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(users);
    }

    @Test
    void rejectsInactiveUser() throws Exception {
        UUID id = UUID.randomUUID();
        var user = new AppUser();
        user.setId(id);
        user.setActive(false);
        when(users.findById(id)).thenReturn(Optional.of(user));
        request(token(id, Instant.now().plusSeconds(60), key));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
