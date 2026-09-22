package backend.auth;

import java.security.SecureRandom;
import java.util.Base64;

public final class JwtSecretGenerator {

    private JwtSecretGenerator() {
    }

    public static void main(String[] args) {
        byte[] secretBytes = new byte[32];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(secretBytes);

        String secret = Base64.getEncoder()
                .encodeToString(secretBytes);

        System.out.println("JWT_SECRET=" + secret);
    }
}