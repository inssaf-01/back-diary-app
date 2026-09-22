package backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "L'identifiant est obligatoire") String identifier,

        @NotBlank(message = "Le mot de passe est obligatoire") @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères") String password,

        boolean rememberMe) {
}