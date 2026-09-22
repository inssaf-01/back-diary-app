package backend.parametre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParametreRepository
        extends JpaRepository<Parametre, Long> {

    List<Parametre> findByCategorieAndActifTrueOrderByOrdreAsc(
            String categorie);
}