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
    @Autowired ParametreRepository parameters;

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
        var inProgress = parameter("STATUT_TACHE", "EN_COURS");
        var done = parameter("STATUT_TACHE", "TERMINEE");
        var first = task(owner, type, original);
        var second = task(owner, type, original);
        var response = service.updateStatuts(
                new UsernamePasswordAuthenticationToken(owner.getId().toString(), null, List.of()),
                new ModificationStatutsRequest(List.of(
                        new ModificationStatutRequest(first.getId(), inProgress.getId()),
                        new ModificationStatutRequest(second.getId(), done.getId()))));
        assertEquals(inProgress.getId(), response.stream().filter(t -> t.id().equals(first.getId())).findFirst().orElseThrow().statutId());
        assertEquals(done.getId(), response.stream().filter(t -> t.id().equals(second.getId())).findFirst().orElseThrow().statutId());
        em.clear();
        assertEquals("EN_COURS", em.find(Tache.class, first.getId()).getStatut().getCode());
        assertEquals("TERMINEE", em.find(Tache.class, second.getId()).getStatut().getCode());
    }

    @Test void rejectsInvalidBatchBeforeChangingAnyTask() {
        var owner = user();
        var type = parameter("TYPE_TACHE", "TACHE");
        var original = parameter("STATUT_TACHE", "A_FAIRE");
        var inProgress = parameter("STATUT_TACHE", "EN_COURS");
        var first = task(owner, type, original);
        var second = task(owner, type, original);
        assertThrows(ResponseStatusException.class, () -> service.updateStatuts(
                new UsernamePasswordAuthenticationToken(owner.getId().toString(), null, List.of()),
                new ModificationStatutsRequest(List.of(
                        new ModificationStatutRequest(first.getId(), inProgress.getId()),
                        new ModificationStatutRequest(second.getId(), type.getId())))));
        em.clear();
        assertEquals("A_FAIRE", em.find(Tache.class, first.getId()).getStatut().getCode());
    }

    @Test void createsAndUpdatesRelationsByIdAndReadsOnlyIds() {
        var owner = user();
        var type = parameter("TYPE_TACHE", "TACHE");
        var status = parameter("STATUT_TACHE", "A_FAIRE");
        var progress = parameter("STATUT_TACHE", "EN_COURS");
        var priority = parameter("PRIORITE_TACHE", "NORMALE");
        var auth = new UsernamePasswordAuthenticationToken(owner.getId().toString(), null, List.of());
        var start = OffsetDateTime.parse("2026-09-23T13:00:00Z");
        var created = service.create(auth, new TacheRequest(type.getId(), status.getId(), priority.getId(),
                "Documentation", "Guide", start, start.plusHours(1), false));
        assertEquals(type.getId(), created.typeTacheId());
        assertEquals(status.getId(), created.statutId());
        assertEquals(priority.getId(), created.prioriteId());
        em.flush();
        em.clear();
        var saved = em.find(Tache.class, created.id());
        assertEquals(type.getId(), saved.getTypeTache().getId());
        assertEquals(priority.getId(), saved.getPriorite().getId());

        service.update(auth, created.id(), new TacheRequest(type.getId(), progress.getId(), null,
                "Documentation modifiée", "Guide", start, null, true));
        em.flush();
        em.clear();
        var read = service.findCalendrier(auth, start.minusDays(1), start.plusDays(1)).get(0);
        assertEquals(progress.getId(), read.statutId());
        assertNull(read.prioriteId());
        assertEquals("Documentation modifiée", read.titre());
        assertEquals(List.of("id", "typeTacheId", "statutId", "prioriteId", "titre", "details",
                "dateDebut", "dateFin", "touteLaJournee", "createdAt", "updatedAt"),
                java.util.Arrays.stream(TacheResponse.class.getRecordComponents()).map(java.lang.reflect.RecordComponent::getName).toList());
    }

    @Test void rejectsWrongCategoryAndDisabledParameterOnWrite() {
        var owner = user();
        var type = parameter("TYPE_TACHE", "TACHE");
        var status = parameter("STATUT_TACHE", "A_FAIRE");
        var auth = new UsernamePasswordAuthenticationToken(owner.getId().toString(), null, List.of());
        var start = OffsetDateTime.now();
        assertThrows(ResponseStatusException.class, () -> service.create(auth,
                new TacheRequest(status.getId(), status.getId(), null, "Test", null, start, null, false)));
        status.setActif(false);
        assertThrows(ResponseStatusException.class, () -> service.create(auth,
                new TacheRequest(type.getId(), status.getId(), null, "Test", null, start, null, false)));
    }

    @Test void returnsAllActiveParameterCategoriesInOneRequest() throws Exception {
        var type = parameter("TYPE_TACHE", "REUNION");
        type.setLibelle("Réunion personnalisée");
        parameter("STATUT_TACHE", "EN_COURS");
        parameter("PRIORITE_TACHE", "NORMALE");
        parameter("AUTRE_CATEGORIE", "AUTRE");
        var inactive = parameter("TYPE_TACHE", "INACTIF");
        inactive.setActif(false);
        em.flush();
        var controller = new backend.parametre.ParametreController(parameters);
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/parametres"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.length()").value(4));
        var all = controller.findByCategorie(null);
        var response = all.stream().filter(p -> p.id().equals(type.getId())).findFirst().orElseThrow();
        assertEquals("REUNION", response.code());
        assertEquals("Réunion personnalisée", response.libelle());
        assertEquals("TYPE_TACHE", response.categorie());
        assertEquals(1, controller.findByCategorie("TYPE_TACHE").size());
    }

    @Test void agendaIncludesOverlappingAndCompletedTasksOnlyForCurrentUser() {
        var owner = user();
        var type = parameter("TYPE_TACHE", "TACHE");
        var active = parameter("STATUT_TACHE", "A_FAIRE");
        var done = parameter("STATUT_TACHE", "TERMINEE");
        var start = OffsetDateTime.parse("2026-09-23T00:00:00Z");
        var overnight = task(owner, type, active);
        overnight.setDateDebut(start.minusHours(1)); overnight.setDateFin(start.plusHours(2));
        var completed = task(owner, type, done);
        completed.setDateDebut(start.plusHours(10));
        var boundaryEnd = task(owner, type, active);
        boundaryEnd.setDateDebut(start.minusHours(2)); boundaryEnd.setDateFin(start);
        var boundaryStart = task(owner, type, active);
        boundaryStart.setDateDebut(start.plusDays(1));
        var allDay = task(owner, type, active);
        allDay.setDateDebut(start.minusDays(1)); allDay.setDateFin(start); allDay.setTouteLaJournee(true);
        var other = new AppUser(); other.setUsername("other"); other.setEmail("other@example.test");
        other.setPasswordHash("unused"); other.setRole(owner.getRole()); em.persist(other);
        var foreign = task(other, type, active); foreign.setDateDebut(start.plusHours(12));
        em.flush(); em.clear();
        var auth = new UsernamePasswordAuthenticationToken(owner.getId().toString(), null, List.of());
        var ids = service.findAgenda(auth, start, start.plusDays(1)).stream().map(TacheResponse::id).toList();
        assertEquals(java.util.Set.of(overnight.getId(), completed.getId(), allDay.getId()), new java.util.HashSet<>(ids));
        assertTrue(service.findCalendrier(auth, start, start.plusDays(1)).isEmpty());
    }
}
