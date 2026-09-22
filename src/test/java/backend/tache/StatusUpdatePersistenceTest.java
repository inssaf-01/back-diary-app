package backend.tache;

import backend.parametre.Parametre;
import backend.parametre.ParametreRepository;
import backend.user.AppRole;
import backend.user.AppUser;
import backend.user.AppUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

// Isolated in-memory database: never connects to the application's PostgreSQL database.
@SpringJUnitConfig(StatusUpdatePersistenceTest.Config.class)
@Transactional
class StatusUpdatePersistenceTest {
    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackages = {"backend.tache", "backend.parametre", "backend.user"})
    static class Config {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:status_updates;DB_CLOSE_DELAY=-1", "sa", "");
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("backend.tache", "backend.parametre", "backend.user");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }
        @Bean JpaTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
        @Bean TacheService tacheService(TacheRepository tasks, ParametreRepository params, AppUserRepository users) {
            return new TacheService(tasks, params, users);
        }
    }

    @PersistenceContext EntityManager em;
    @Autowired TacheService service;

    Parametre parameter(String category, String code) {
        var p = new Parametre();
        p.setCategorie(category); p.setCode(code); p.setLibelle(code);
        em.persist(p);
        return p;
    }

    AppUser user() {
        var role = new AppRole(); role.setCode(UUID.randomUUID().toString().substring(0, 20)); role.setLabel("Test");
        em.persist(role);
        var user = new AppUser(); user.setUsername("test"); user.setEmail("test@example.test");
        user.setPasswordHash("unused"); user.setRole(role); em.persist(user);
        return user;
    }

    Tache task(AppUser user, Parametre type, Parametre status) {
        var task = new Tache(); task.setUtilisateur(user); task.setTypeTache(type); task.setStatut(status);
        task.setTitre("Test"); task.setDateDebut(OffsetDateTime.now()); em.persist(task); em.flush();
        return task;
    }

    @Test void returnsFreshStatusesAfterBulkUpdateInTheSamePersistenceContext() {
        var owner = user();
        var type = parameter("TYPE_TACHE", "TACHE");
        var original = parameter("STATUT_TACHE", "A_FAIRE");
        parameter("STATUT_TACHE", "EN_COURS");
        parameter("STATUT_TACHE", "TERMINEE");
        var first = task(owner, type, original);
        var second = task(owner, type, original);
        var response = service.updateStatuts(
                new UsernamePasswordAuthenticationToken(owner.getId().toString(), null, List.of()),
                new ModificationStatutsRequest(List.of(
                        new ModificationStatutRequest(first.getId(), "EN_COURS"),
                        new ModificationStatutRequest(second.getId(), "TERMINEE"))));
        assertEquals("EN_COURS", response.stream().filter(t -> t.id().equals(first.getId())).findFirst().orElseThrow().statut().code());
        assertEquals("TERMINEE", response.stream().filter(t -> t.id().equals(second.getId())).findFirst().orElseThrow().statut().code());
        em.clear();
        assertEquals("EN_COURS", em.find(Tache.class, first.getId()).getStatut().getCode());
        assertEquals("TERMINEE", em.find(Tache.class, second.getId()).getStatut().getCode());
    }

    @Test void rejectsInvalidBatchBeforeChangingAnyTask() {
        var owner = user();
        var type = parameter("TYPE_TACHE", "TACHE");
        var original = parameter("STATUT_TACHE", "A_FAIRE");
        parameter("STATUT_TACHE", "EN_COURS");
        var first = task(owner, type, original);
        var second = task(owner, type, original);
        assertThrows(ResponseStatusException.class, () -> service.updateStatuts(
                new UsernamePasswordAuthenticationToken(owner.getId().toString(), null, List.of()),
                new ModificationStatutsRequest(List.of(
                        new ModificationStatutRequest(first.getId(), "EN_COURS"),
                        new ModificationStatutRequest(second.getId(), "INVALID")))));
        em.clear();
        assertEquals("A_FAIRE", em.find(Tache.class, first.getId()).getStatut().getCode());
    }
}
