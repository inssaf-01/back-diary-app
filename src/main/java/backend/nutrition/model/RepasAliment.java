package backend.nutrition.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "repas_aliment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepasAliment {

    @EmbeddedId
    @Builder.Default
    private RepasAlimentId id = new RepasAlimentId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("repasId")
    @JoinColumn(name = "repas_id")
    private Repas repas;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("alimentId")
    @JoinColumn(name = "aliment_id")
    private Aliment aliment;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantite;
}