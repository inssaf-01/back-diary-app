package backend.tache;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import backend.parametre.Parametre;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TacheRepository extends JpaRepository<Tache, UUID> {

    List<Tache> findAllByIdInAndUtilisateurId(List<UUID> ids, UUID utilisateurId);

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

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                UPDATE Tache t
                SET t.statut = :statut
                WHERE t.id = :tacheId
                  AND t.utilisateur.id = :utilisateurId
            """)
    int updateStatut(
            @Param("tacheId") UUID tacheId,
            @Param("utilisateurId") UUID utilisateurId,
            @Param("statut") Parametre statut);
}
