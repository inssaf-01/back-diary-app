package backend.tache;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import backend.parametre.Parametre;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TacheRepository extends JpaRepository<Tache, UUID> {

    @EntityGraph(attributePaths = {"typeTache", "statut", "priorite"})
    @Query("""
            SELECT t FROM Tache t
            WHERE t.utilisateur.id = :utilisateurId
              AND t.dateDebut < :dateFin
              AND (t.dateDebut >= :dateDebut OR t.dateFin > :dateDebut
                   OR (t.touteLaJournee = true AND t.dateFin >= :dateDebut))
            ORDER BY t.dateDebut ASC
            """)
    List<Tache> findAgenda(@Param("utilisateurId") UUID utilisateurId,
            @Param("dateDebut") OffsetDateTime dateDebut, @Param("dateFin") OffsetDateTime dateFin);

    @EntityGraph(attributePaths = {"typeTache", "statut", "priorite"})
    List<Tache> findAllByIdInAndUtilisateurId(List<UUID> ids, UUID utilisateurId);

    @EntityGraph(attributePaths = {"typeTache", "statut", "priorite"})
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
