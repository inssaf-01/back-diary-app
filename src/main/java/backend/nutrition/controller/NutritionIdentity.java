package backend.nutrition.controller;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
final class NutritionIdentity {
    private NutritionIdentity() {}
    static UUID userId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        try { return UUID.fromString(auth.getName()); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED); }
    }
    static UUID requireOwner(Authentication auth, UUID requested) {
        var id = userId(auth);
        if (!id.equals(requested)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return id;
    }
}
