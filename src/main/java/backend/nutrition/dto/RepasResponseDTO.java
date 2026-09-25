package backend.nutrition.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RepasResponseDTO {
    private UUID id;
    private UUID utilisateurId;
    private String nom;
    private String description;
    private String typeRepas;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Totaux nutritionnels calculés pour le repas
    private BigDecimal totalCalories;
    private BigDecimal totalProteinesG;
    private BigDecimal totalGlucidesG;
    private BigDecimal totalLipidesG;
    private BigDecimal totalFibresG;

    private List<AlimentDetailDTO> aliments;
}