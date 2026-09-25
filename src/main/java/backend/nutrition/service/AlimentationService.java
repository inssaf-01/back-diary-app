package backend.nutrition.service;

import backend.nutrition.dto.AlimentationDashboardDTO;
import backend.nutrition.dto.AlimentationDashboardDTO.*;
import backend.nutrition.model.Aliment;
import backend.nutrition.model.Repas;
import backend.nutrition.repository.AlimentRepository;
import backend.nutrition.repository.RepasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlimentationService {

        private final AlimentRepository alimentRepository;
        private final RepasRepository repasRepository;

        @Transactional(readOnly = true)
        public AlimentationDashboardDTO getDashboardData(UUID utilisateurId) {

                // 1. Récupération des aliments via la méthode déclarée dans AlimentRepository
                List<Aliment> aliments = alimentRepository.findAllByUtilisateurIdOrderByNomAsc(utilisateurId);

                List<AlimentDTO> tousLesAlimentsDTO = aliments.stream()
                                .map(this::mapToAlimentDTO)
                                .collect(Collectors.toList());

                // 2. Filtrage pour le Stock (quantité stock > 0)
                List<AlimentDTO> enStock = tousLesAlimentsDTO.stream()
                                .filter(a -> a.getQuantiteStock() != null
                                                && a.getQuantiteStock().compareTo(BigDecimal.ZERO) > 0)
                                .collect(Collectors.toList());

                // 3. Filtrage pour la Liste de Courses (quantité à acheter > 0)
                List<AlimentDTO> aAcheter = tousLesAlimentsDTO.stream()
                                .filter(a -> a.getQuantiteAAcheter() != null
                                                && a.getQuantiteAAcheter().compareTo(BigDecimal.ZERO) > 0)
                                .collect(Collectors.toList());

                // 4. Récupération des Repas via la requête custom optimisée (LEFT JOIN FETCH)
                List<Repas> repasList = repasRepository.findAllByUtilisateurIdWithAliments(utilisateurId);

                List<RepasCompletDTO> repasDTOs = repasList.stream().map(repas -> {
                        List<IngredientDTO> ingredients = repas.getRepasAliments().stream()
                                        .filter(ra -> utilisateurId.equals(ra.getAliment().getUtilisateur().getId()))
                                        .map(ra -> IngredientDTO.builder()
                                                        .alimentId(ra.getAliment().getId())
                                                        .nomAliment(ra.getAliment().getNom())
                                                        .quantite(ra.getQuantite())
                                                        .unite(ra.getAliment().getUnite())
                                                        .build())
                                        .collect(Collectors.toList());

                        return RepasCompletDTO.builder()
                                        .id(repas.getId())
                                        .nom(repas.getNom())
                                        .description(repas.getDescription())
                                        .typeRepas(repas.getTypeRepas())
                                        .ingredients(ingredients)
                                        .build();
                }).collect(Collectors.toList());

                return AlimentationDashboardDTO.builder()
                                .tousLesAliments(tousLesAlimentsDTO)
                                .alimentsEnStock(enStock)
                                .alimentsAAcheter(aAcheter)
                                .repas(repasDTOs)
                                .build();
        }

        private AlimentDTO mapToAlimentDTO(Aliment a) {
                String emplacement = (a.getQuantiteStock() != null
                                && a.getQuantiteStock().compareTo(BigDecimal.ZERO) > 0)
                                                ? "stock"
                                                : "courses";

                return AlimentDTO.builder()
                                .id(a.getId())
                                .utilisateurId(a.getUtilisateur().getId())
                                .nom(a.getNom())
                                .description(a.getDescription())
                                .unite(a.getUnite())
                                .quantiteStock(a.getQuantiteStock())
                                .quantiteAAcheter(a.getQuantiteAAcheter())
                                .calories(a.getCalories())
                                .proteinesG(a.getProteinesG())
                                .glucidesG(a.getGlucidesG())
                                .lipidesG(a.getLipidesG())
                                .fibresG(a.getFibresG())
                                .baseNutritionnelle(a.getBaseNutritionnelle())
                                .emplacement(emplacement)
                                .build();
        }
}