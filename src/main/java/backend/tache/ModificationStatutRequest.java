package backend.tache;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ModificationStatutRequest(
        @NotNull UUID tacheId,
        @NotBlank @Size(max = 50) String statutCode) {
}
