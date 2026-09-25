package backend.nutrition.controller;
import backend.nutrition.model.Aliment;
import backend.nutrition.repository.AlimentRepository;
import backend.nutrition.service.RepasService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class NutritionAccessTest {
    @Test void dashboardReturnsStockShoppingAndMealIngredientsInOneResponse() throws Exception {
        var foods = mock(AlimentRepository.class);
        var meals = mock(backend.nutrition.repository.RepasRepository.class);
        var owner = new backend.user.AppUser(); owner.setId(UUID.randomUUID());
        var food = Aliment.builder().id(UUID.randomUUID()).utilisateur(owner).nom("Pomme").unite("g")
            .quantiteStock(new java.math.BigDecimal("100")).quantiteAAcheter(new java.math.BigDecimal("50")).build();
        var meal = backend.nutrition.model.Repas.builder().id(UUID.randomUUID()).utilisateur(owner).nom("Salade").build();
        meal.getRepasAliments().add(backend.nutrition.model.RepasAliment.builder().repas(meal).aliment(food).quantite(new java.math.BigDecimal("25")).build());
        when(foods.findAllByUtilisateurIdOrderByNomAsc(owner.getId())).thenReturn(List.of(food));
        when(meals.findAllByUtilisateurIdWithAliments(owner.getId())).thenReturn(List.of(meal));
        var service = new backend.nutrition.service.AlimentationService(foods, meals);
        var mvc = MockMvcBuilders.standaloneSetup(new AlimentationController(service)).build();
        var auth = new UsernamePasswordAuthenticationToken(owner.getId().toString(), null, List.of());
        mvc.perform(get("/api/alimentation/dashboard").principal(auth)).andExpect(status().isOk())
            .andExpect(jsonPath("$.tousLesAliments.length()").value(1))
            .andExpect(jsonPath("$.alimentsEnStock[0].quantiteStock").value(100))
            .andExpect(jsonPath("$.alimentsAAcheter[0].quantiteAAcheter").value(50))
            .andExpect(jsonPath("$.repas[0].ingredients[0].alimentId").value(food.getId().toString()))
            .andExpect(jsonPath("$.repas[0].ingredients[0].quantite").value(25));
        mvc.perform(get("/api/alimentation/dashboard/user/" + UUID.randomUUID()).principal(auth)).andExpect(status().isForbidden());
        mvc.perform(get("/api/alimentation/dashboard")).andExpect(status().isUnauthorized());
        verify(foods).findAllByUtilisateurIdOrderByNomAsc(owner.getId());
        verify(meals).findAllByUtilisateurIdWithAliments(owner.getId());
        verifyNoMoreInteractions(foods, meals);
        org.junit.jupiter.api.Assertions.assertEquals(java.math.BigDecimal.ZERO, Aliment.builder().build().getQuantiteAAcheter());
        org.junit.jupiter.api.Assertions.assertNotNull(backend.nutrition.model.RepasAliment.builder().build().getId());
    }
    @Test void foodReadUsesAuthenticatedOwnerAndDoesNotExposeUserEntity() throws Exception {
        var repository = mock(AlimentRepository.class);
        var owner = UUID.randomUUID();
        var food = new Aliment(); food.setId(UUID.randomUUID()); food.setNom("Pomme"); food.setUnite("g");
        when(repository.findAllByUtilisateurIdOrderByNomAsc(owner)).thenReturn(List.of(food));
        var mvc = MockMvcBuilders.standaloneSetup(new AlimentController(repository, mock(backend.user.AppUserRepository.class))).build();
        var auth = new UsernamePasswordAuthenticationToken(owner.toString(), null, List.of());
        mvc.perform(get("/api/aliments").principal(auth)).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nom").value("Pomme")).andExpect(jsonPath("$[0].utilisateur").doesNotExist());
        verify(repository).findAllByUtilisateurIdOrderByNomAsc(owner);
        mvc.perform(get("/api/aliments/user/" + UUID.randomUUID()).principal(auth)).andExpect(status().isForbidden());
        mvc.perform(get("/api/aliments")).andExpect(status().isUnauthorized());
        verifyNoMoreInteractions(repository);
    }
    @Test void mealReadRejectsAnotherUsersIdBeforeCallingService() throws Exception {
        var service = mock(RepasService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new RepasController(service)).build();
        var owner = UUID.randomUUID();
        var auth = new UsernamePasswordAuthenticationToken(owner.toString(), null, List.of());
        mvc.perform(get("/api/repas/user/" + UUID.randomUUID()).principal(auth)).andExpect(status().isForbidden());
        mvc.perform(get("/api/repas/" + UUID.randomUUID() + "/user/" + UUID.randomUUID()).principal(auth)).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
}
