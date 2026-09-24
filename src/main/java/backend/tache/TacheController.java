package backend.tache;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/taches")
public class TacheController {

    private final TacheService tacheService;

    public TacheController(TacheService tacheService) {
        this.tacheService = tacheService;
    }

    @GetMapping("/accueil")
    public TachePageResponse getAccueil(Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFin,
            @RequestParam(defaultValue = "true") boolean inclureRetard,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "3") int size) {
        return tacheService.findAccueil(authentication, dateDebut, dateFin, inclureRetard, page, size);
    }

    @GetMapping("/actives")
    public List<TacheResponse> getActives(Authentication authentication) {
        return tacheService.findActives(authentication);
    }

    @GetMapping("/calendrier")
    public List<TacheResponse> getCalendrier(
            Authentication authentication,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateDebut,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFin,
            @RequestParam(defaultValue = "false") boolean vueComplete) {
        if (vueComplete) return tacheService.findAgenda(authentication, dateDebut, dateFin);
        return tacheService.findCalendrier(
                authentication,
                dateDebut,
                dateFin);
    }

    @PatchMapping("/statuts")
    public List<TacheResponse> updateStatuts(
            Authentication authentication,
            @Valid @RequestBody ModificationStatutsRequest request) {
        return tacheService.updateStatuts(authentication, request);
    }

    @GetMapping("/{id}")
    public TacheResponse getById(
            Authentication authentication,
            @PathVariable UUID id) {
        return tacheService.findById(
                authentication,
                id);
    }

    @PostMapping
    public ResponseEntity<TacheResponse> create(
            Authentication authentication,
            @Valid @RequestBody TacheRequest request) {
        TacheResponse response = tacheService.create(
                authentication,
                request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public TacheResponse update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody TacheRequest request) {
        return tacheService.update(
                authentication,
                id,
                request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            Authentication authentication,
            @PathVariable UUID id) {
        tacheService.delete(
                authentication,
                id);
    }
}
