package backend.tache;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TacheRepository extends JpaRepository<Tache, UUID> {

    @Query("""
                SELECT t
                FROM Tache t
                WHERE t.utilisateur.id = :utilisateurId
                  AND t.dateDebut >= :dateDebut
                  AND t.dateDebut < :dateFin
                  AND t.statut.code NOT IN ('TERMINEE', 'ANNULEE')
                ORDER BY t.dateDebut ASC
            """)
    List<Tache> findCalendrier(
            @Param("utilisateurId") UUID utilisateurId,
            @Param("dateDebut") OffsetDateTime dateDebut,
            @Param("dateFin") OffsetDateTime dateFin);

    Optional<Tache> findByIdAndUtilisateurId(
            UUID id,
            UUID utilisateurId);
}