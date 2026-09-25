package backend.nutrition.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepasAlimentId implements Serializable {

    @Column(name = "repas_id")
    private UUID repasId;

    @Column(name = "aliment_id")
    private UUID alimentId;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RepasAlimentId that = (RepasAlimentId) o;
        return Objects.equals(repasId, that.repasId) && Objects.equals(alimentId, that.alimentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repasId, alimentId);
    }
}
