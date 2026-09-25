package backend.nutrition.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import backend.user.AppUser;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "aliment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aliment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private AppUser utilisateur;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String unite;

    @Builder.Default
    @Column(name = "quantite_stock", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteStock = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "quantite_a_acheter", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteAAcheter = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal calories;

    @Column(name = "proteines_g", precision = 10, scale = 2)
    private BigDecimal proteinesG;

    @Column(name = "glucides_g", precision = 10, scale = 2)
    private BigDecimal glucidesG;

    @Column(name = "lipides_g", precision = 10, scale = 2)
    private BigDecimal lipidesG;

    @Column(name = "fibres_g", precision = 10, scale = 2)
    private BigDecimal fibresG;

    @Column(name = "base_nutritionnelle", length = 20)
    private String baseNutritionnelle;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}