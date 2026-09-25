package backend.nutrition.dto;
import backend.nutrition.model.Aliment;
import java.math.BigDecimal;
import java.util.UUID;
public record AlimentResponse(UUID id, String nom, String description, String unite,
    BigDecimal quantiteStock, BigDecimal quantiteAAcheter, BigDecimal calories,
    String baseNutritionnelle, BigDecimal proteinesG, BigDecimal glucidesG, BigDecimal lipidesG) {
    public static AlimentResponse from(Aliment a) {
        return new AlimentResponse(a.getId(), a.getNom(), a.getDescription(), a.getUnite(), a.getQuantiteStock(),
            a.getQuantiteAAcheter(), a.getCalories(), a.getBaseNutritionnelle(), a.getProteinesG(), a.getGlucidesG(), a.getLipidesG());
    }
}
