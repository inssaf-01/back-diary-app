package backend.nutrition.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlimentationDashboardDTO {
    private List<AlimentDTO> tousLesAliments;
    private List<AlimentDTO> alimentsEnStock;
    private List<AlimentDTO> alimentsAAcheter;
    private List<RepasCompletDTO> repas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlimentDTO {
        private UUID id;
        private UUID utilisateurId;
        private String nom;
        private String description;
        private String unite;
        private BigDecimal quantiteStock;
        private BigDecimal quantiteAAcheter;
        private BigDecimal calories;
        private BigDecimal proteinesG;
        private BigDecimal glucidesG;
        private BigDecimal lipidesG;
        private BigDecimal fibresG;
        private String baseNutritionnelle;
        private String emplacement; // Helper calculé : 'stock' si quantiteStock > 0 sinon 'courses'
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepasCompletDTO {
        private UUID id;
        private String nom;
        private String description;
        private String typeRepas;
        private List<IngredientDTO> ingredients;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDTO {
        private UUID alimentId;
        private String nomAliment;
        private BigDecimal quantite;
        private String unite;
    }
}