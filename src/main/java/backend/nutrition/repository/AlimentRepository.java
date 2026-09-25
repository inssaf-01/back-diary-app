package backend.nutrition.repository;

import backend.nutrition.model.Aliment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AlimentRepository extends JpaRepository<Aliment, UUID> {


    java.util.Optional<Aliment> findByIdAndUtilisateurId(UUID id, UUID utilisateurId);

    List<Aliment> findAllByUtilisateurIdOrderByNomAsc(UUID utilisateurId);
}