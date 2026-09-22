package backend.tache;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record TacheRequest(

        @NotNull(message = "Le type de tâche est obligatoire") Long typeTacheId,

        @NotNull(message = "Le statut est obligatoire") Long statutId,

        Long prioriteId,

        @NotBlank(message = "Le titre est obligatoire") @Size(max = 150, message = "Le titre ne doit pas dépasser 150 caractères") String titre,

        @Size(max = 3000, message = "Les détails ne doivent pas dépasser 3000 caractères") String details,

        @NotNull(message = "La date de début est obligatoire") OffsetDateTime dateDebut,

        OffsetDateTime dateFin,

        boolean touteLaJournee

) {
}