package backend.nutrition.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class AlimentDetailDTO {
    private UUID alimentId;
    private String nom;
    private String unite;
    private BigDecimal quantiteUtilisee;
    private String baseNutritionnelle;
    private BigDecimal caloriesCalculated;
    private BigDecimal proteinesGCalculated;
    private BigDecimal glucidesGCalculated;
    private BigDecimal lipidesGCalculated;
    private BigDecimal fibresGCalculated;
}
