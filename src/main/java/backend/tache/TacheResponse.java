package backend.tache;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TacheResponse(

        UUID id,

        Long typeTacheId,

        Long statutId,

        Long prioriteId,

        String titre,

        String details,

        OffsetDateTime dateDebut,

        OffsetDateTime dateFin,

        boolean touteLaJournee,

        Instant createdAt,

        Instant updatedAt

) {

}
