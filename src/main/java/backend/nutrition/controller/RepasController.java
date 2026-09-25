package backend.nutrition.controller;

import backend.nutrition.dto.RepasResponseDTO;
import backend.nutrition.service.AlimentationService;
import backend.nutrition.service.RepasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repas")
@RequiredArgsConstructor
public class RepasController {

    private final RepasService repasService;

    // GET /api/repas/user/{userId} - Récupère tous les repas d'un utilisateur avec
    // leurs totaux calculés
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RepasResponseDTO>> getRepasByUser(Authentication auth, @PathVariable UUID userId) {
        List<RepasResponseDTO> repas = repasService.getRepasByUtilisateur(NutritionIdentity.requireOwner(auth, userId));
        return ResponseEntity.ok(repas);
    }

    // GET /api/repas/{repasId}/user/{userId} - Récupère un repas spécifique d'un
    // utilisateur
    @GetMapping("/{repasId}/user/{userId}")
    public ResponseEntity<RepasResponseDTO> getRepasById(
            Authentication auth,
            @PathVariable UUID repasId,
            @PathVariable UUID userId) {
        RepasResponseDTO repas = repasService.getRepasByIdAndUtilisateur(repasId,
                NutritionIdentity.requireOwner(auth, userId));
        return ResponseEntity.ok(repas);
    }
}