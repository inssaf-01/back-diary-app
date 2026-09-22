package backend.auth;

import backend.auth.dto.AuthResponse;
import backend.auth.dto.LoginRequest;
import backend.auth.dto.UserResponse;
import backend.user.AppUser;
import backend.user.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(
            LoginRequest request,
            HttpServletResponse response) {
        String identifier = request.identifier().trim();

        AppUser user = userRepository
                .findByEmailIgnoreCaseOrUsernameIgnoreCase(
                        identifier,
                        identifier)
                .orElseThrow(() -> new BadCredentialsException(
                        "Identifiant ou mot de passe incorrect"));

        if (!user.isActive()) {
            throw new BadCredentialsException(
                    "Ce compte utilisateur est désactivé");
        }

        System.out.println("1 - Utilisateur trouvé");

        boolean validPassword = passwordEncoder.matches(
                request.password(),
                user.getPasswordHash());

        System.out.println(
                "2 - Résultat du mot de passe : "
                        + validPassword);

        if (!validPassword) {
            throw new BadCredentialsException(
                    "Identifiant ou mot de passe incorrect");
        }

        System.out.println("3 - Génération du JWT");

        String accessToken = jwtService.generateAccessToken(user);

        System.out.println("4 - JWT généré");

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                UserResponse.from(user));
    }

    public AuthResponse refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        throw new UnsupportedOperationException(
                "Le refresh token sera ajouté ensuite");
    }

    public void logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        // Cette partie sera complétée avec le refresh token.
    }

    public UserResponse getCurrentUser() {
        throw new UnsupportedOperationException(
                "La récupération depuis SecurityContext sera ajoutée ensuite");
    }
}