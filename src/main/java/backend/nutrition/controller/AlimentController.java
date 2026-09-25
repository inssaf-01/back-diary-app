package backend.nutrition.controller;
import backend.nutrition.dto.AlimentResponse;
import backend.nutrition.model.Aliment;
import backend.nutrition.repository.AlimentRepository;
import backend.user.AppUserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
@RestController
@RequestMapping("/api/aliments")
public class AlimentController {
    private final AlimentRepository aliments;
    private final AppUserRepository users;
    public AlimentController(AlimentRepository aliments, AppUserRepository users) { this.aliments = aliments; this.users = users; }
    @GetMapping
    public List<AlimentResponse> list(Authentication auth) {
        return aliments.findAllByUtilisateurIdOrderByNomAsc(NutritionIdentity.userId(auth)).stream().map(AlimentResponse::from).toList();
    }
    @GetMapping("/user/{userId}")
    public List<AlimentResponse> legacyList(Authentication auth, @PathVariable UUID userId) {
        NutritionIdentity.requireOwner(auth, userId); return list(auth);
    }
    public record Creation(@NotBlank @Size(max=150) String nom, String description, @NotBlank @Size(max=20) String unite,
        @NotNull @DecimalMin("0") BigDecimal quantiteStock, @NotNull @DecimalMin("0") BigDecimal quantiteAAcheter) {}
    public record Quantite(@NotNull @DecimalMin("0") BigDecimal quantite, @NotBlank @Pattern(regexp="stock|courses") String emplacement) {}
    public record Emplacement(@NotBlank @Pattern(regexp="stock") String emplacement) {}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional
    public AlimentResponse create(Authentication auth, @Valid @RequestBody Creation body) {
        var user = users.findById(NutritionIdentity.userId(auth)).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var a = new Aliment(); a.setUtilisateur(user); a.setNom(body.nom().trim()); a.setDescription(body.description()); a.setUnite(body.unite());
        a.setQuantiteStock(body.quantiteStock()); a.setQuantiteAAcheter(body.quantiteAAcheter());
        return AlimentResponse.from(aliments.save(a));
    }
    private Aliment owned(Authentication auth, UUID id) {
        return aliments.findByIdAndUtilisateurId(id, NutritionIdentity.userId(auth)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    @PatchMapping("/{id}/quantite") @Transactional
    public AlimentResponse quantity(Authentication auth, @PathVariable UUID id, @Valid @RequestBody Quantite body) {
        var a = owned(auth, id);
        if (body.emplacement().equals("stock")) a.setQuantiteStock(body.quantite()); else a.setQuantiteAAcheter(body.quantite());
        return AlimentResponse.from(aliments.save(a));
    }
    @PatchMapping("/{id}/emplacement") @Transactional
    public AlimentResponse move(Authentication auth, @PathVariable UUID id, @Valid @RequestBody Emplacement body) {
        var a = owned(auth, id); a.setQuantiteStock(a.getQuantiteStock().add(a.getQuantiteAAcheter())); a.setQuantiteAAcheter(BigDecimal.ZERO);
        return AlimentResponse.from(aliments.save(a));
    }
}
