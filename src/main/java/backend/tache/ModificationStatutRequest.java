package backend.tache;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record ModificationStatutRequest(
        @NotNull UUID tacheId,
        @NotNull @Positive Long statutId) {
}
