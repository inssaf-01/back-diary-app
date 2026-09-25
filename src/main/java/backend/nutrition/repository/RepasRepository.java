package backend.nutrition.repository;

import backend.nutrition.model.Repas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepasRepository extends JpaRepository<Repas, UUID> {

    // Jointure pour éviter le problème N+1 Select lors de la récupération
    @Query("SELECT DISTINCT r FROM Repas r " +
            "LEFT JOIN FETCH r.repasAliments ra " +
            "LEFT JOIN FETCH ra.aliment " +
            "WHERE r.utilisateur.id = :userId")
    List<Repas> findAllByUtilisateurIdWithAliments(@Param("userId") UUID userId);

    @Query("SELECT r FROM Repas r " +
            "LEFT JOIN FETCH r.repasAliments ra " +
            "LEFT JOIN FETCH ra.aliment " +
            "WHERE r.id = :repasId AND r.utilisateur.id = :userId")
    Optional<Repas> findByIdAndUtilisateurId(@Param("repasId") UUID repasId, @Param("userId") UUID userId);
}