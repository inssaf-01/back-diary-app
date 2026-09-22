package backend.parametre;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parametres")
public class ParametreController {

    private final ParametreRepository parametreRepository;

    public ParametreController(
            ParametreRepository parametreRepository) {
        this.parametreRepository = parametreRepository;
    }

    @GetMapping
    public List<ParametreResponse> findByCategorie(
            @RequestParam String categorie) {
        return parametreRepository
                .findByCategorieAndActifTrueOrderByOrdreAsc(
                        categorie.trim().toUpperCase())
                .stream()
                .map(parametre -> new ParametreResponse(
                        parametre.getId(),
                        parametre.getCategorie(),
                        parametre.getCode(),
                        parametre.getLibelle(),
                        parametre.getOrdre()))
                .toList();
    }

    public record ParametreResponse(
            Long id,
            String categorie,
            String code,
            String libelle,
            int ordre) {
    }
}