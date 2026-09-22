package backend.tache;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ModificationStatutsRequest(
        @NotEmpty List<@NotNull @Valid ModificationStatutRequest> modifications) {
}
