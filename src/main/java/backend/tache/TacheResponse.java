package backend.tache;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TacheResponse(

        UUID id,

        ParametreInfo typeTache,

        ParametreInfo statut,

        ParametreInfo priorite,

        String titre,

        String details,

        OffsetDateTime dateDebut,

        OffsetDateTime dateFin,

        boolean touteLaJournee,

        Instant createdAt,

        Instant updatedAt

) {

    public record ParametreInfo(
            Long id,
            String code,
            String libelle) {
    }
}