package backend.nutrition.service;
import backend.nutrition.dto.*;
import backend.nutrition.model.*;
import backend.nutrition.repository.RepasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.math.BigDecimal;
import java.util.function.Function;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepasService {
    private final RepasRepository repasRepository;
    public List<RepasResponseDTO> getRepasByUtilisateur(UUID owner) {
        return repasRepository.findAllByUtilisateurIdWithAliments(owner).stream().map(r -> response(r, owner)).toList();
    }
    public RepasResponseDTO getRepasByIdAndUtilisateur(UUID id, UUID owner) {
        return response(repasRepository.findByIdAndUtilisateurId(id, owner).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)), owner);
    }
    private BigDecimal macro(BigDecimal value, BigDecimal factor) { return value == null ? BigDecimal.ZERO : value.multiply(factor); }
    private BigDecimal sum(List<AlimentDetailDTO> rows, Function<AlimentDetailDTO, BigDecimal> getter) {
        return rows.stream().map(getter).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private RepasResponseDTO response(Repas r, UUID owner) {
        var ingredients = r.getRepasAliments().stream().filter(ra -> owner.equals(ra.getAliment().getUtilisateur().getId())).map(ra -> {
            var a = ra.getAliment();
            var factor = ra.getQuantite();
            if ("100g".equalsIgnoreCase(a.getBaseNutritionnelle()) || "100ml".equalsIgnoreCase(a.getBaseNutritionnelle())) factor = factor.movePointLeft(2);
            return AlimentDetailDTO.builder().alimentId(a.getId()).nom(a.getNom()).unite(a.getUnite()).quantiteUtilisee(ra.getQuantite())
                .baseNutritionnelle(a.getBaseNutritionnelle()).caloriesCalculated(macro(a.getCalories(), factor))
                .proteinesGCalculated(macro(a.getProteinesG(), factor)).glucidesGCalculated(macro(a.getGlucidesG(), factor))
                .lipidesGCalculated(macro(a.getLipidesG(), factor)).fibresGCalculated(macro(a.getFibresG(), factor)).build();
        }).toList();
        return RepasResponseDTO.builder().id(r.getId()).utilisateurId(owner).nom(r.getNom()).description(r.getDescription()).typeRepas(r.getTypeRepas())
            .createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt()).aliments(ingredients)
            .totalCalories(sum(ingredients, AlimentDetailDTO::getCaloriesCalculated)).totalProteinesG(sum(ingredients, AlimentDetailDTO::getProteinesGCalculated))
            .totalGlucidesG(sum(ingredients, AlimentDetailDTO::getGlucidesGCalculated)).totalLipidesG(sum(ingredients, AlimentDetailDTO::getLipidesGCalculated))
            .totalFibresG(sum(ingredients, AlimentDetailDTO::getFibresGCalculated)).build();
    }
}
