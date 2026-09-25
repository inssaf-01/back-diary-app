package backend.nutrition.controller;

import backend.nutrition.dto.AlimentationDashboardDTO;
import backend.nutrition.service.AlimentationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/alimentation")
@RequiredArgsConstructor
public class AlimentationController {

    private final AlimentationService alimentationService;

    @GetMapping("/dashboard")
    public ResponseEntity<AlimentationDashboardDTO> getMyDashboard(Authentication auth) {
        return ResponseEntity.ok(alimentationService.getDashboardData(NutritionIdentity.userId(auth)));
    }

    @GetMapping("/dashboard/user/{userId}")
    public ResponseEntity<AlimentationDashboardDTO> getDashboardByUserId(Authentication auth,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(alimentationService.getDashboardData(NutritionIdentity.requireOwner(auth, userId)));
    }
}
