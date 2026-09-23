package backend.tache;

import backend.parametre.Parametre;
import backend.parametre.ParametreRepository;
import backend.user.AppUser;
import backend.user.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class TacheService {

    private final TacheRepository tacheRepository;
    private final ParametreRepository parametreRepository;
    private final AppUserRepository appUserRepository;

    public TacheService(
            TacheRepository tacheRepository,
            ParametreRepository parametreRepository,
            AppUserRepository appUserRepository) {
        this.tacheRepository = tacheRepository;
        this.parametreRepository = parametreRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<TacheResponse> findCalendrier(
            Authentication authentication,
            OffsetDateTime dateDebut,
            OffsetDateTime dateFin) {
        verifierPeriode(dateDebut, dateFin);

        AppUser utilisateur = getUtilisateurConnecte(authentication);

        return tacheRepository
                .findCalendrier(
                        utilisateur.getId(),
                        dateDebut,
                        dateFin)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TacheResponse> findAgenda(Authentication authentication, OffsetDateTime dateDebut, OffsetDateTime dateFin) {
        verifierPeriode(dateDebut, dateFin);
        var utilisateur = getUtilisateurConnecte(authentication);
        return tacheRepository.findAgenda(utilisateur.getId(), dateDebut, dateFin).stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<TacheResponse> updateStatuts(
            Authentication authentication,
            ModificationStatutsRequest request) {

        AppUser utilisateur = getUtilisateurConnecte(authentication);

        if (request == null
                || request.modifications() == null
                || request.modifications().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La liste ne doit pas être vide");
        }

        Map<UUID, Long> modifications = new LinkedHashMap<>();

        for (ModificationStatutRequest modification : request.modifications()) {

            if (modification == null
                    || modification.tacheId() == null
                    || modification.statutId() == null
                    || modification.statutId() <= 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Modification invalide");
            }

            if (modifications.putIfAbsent(
                    modification.tacheId(),
                    modification.statutId()) != null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Une tâche apparaît plusieurs fois");
            }
        }

        List<UUID> ids = List.copyOf(modifications.keySet());

        // Vérifie que toutes les tâches appartiennent à l'utilisateur connecté.
        List<Tache> taches = tacheRepository
                .findAllByIdInAndUtilisateurId(
                        ids,
                        utilisateur.getId());

        if (taches.size() != modifications.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Une ou plusieurs tâches sont introuvables");
        }

        // Charge tous les statuts demandés en une seule requête.
        Map<Long, Parametre> statuts = parametreRepository
                .findAllById(modifications.values())
                .stream()
                .collect(Collectors.toMap(
                        Parametre::getId,
                        Function.identity()));

        // Valide tous les statuts avant toute modification.
        for (Long id : modifications.values()) {

            Parametre statut = statuts.get(id);

            if (statut == null || !statut.isActif() || !"STATUT_TACHE".equals(statut.getCategorie())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Statut invalide ou désactivé");
            }
        }

        // Mise à jour ciblée : uniquement statut_id.
        for (Map.Entry<UUID, Long> modification : modifications.entrySet()) {

            Parametre statut = statuts.get(modification.getValue());

            int updated = tacheRepository.updateStatut(
                    modification.getKey(),
                    utilisateur.getId(),
                    statut);
            if (updated != 1) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Une tâche n'est plus disponible");
            }
        }

        // Recharge uniquement les tâches concernées pour retourner la réponse.
        List<Tache> tachesModifiees = tacheRepository
                .findAllByIdInAndUtilisateurId(
                        ids,
                        utilisateur.getId());

        return tachesModifiees
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TacheResponse findById(
            Authentication authentication,
            UUID id) {
        return toResponse(
                getTacheUtilisateur(authentication, id));
    }

    public TacheResponse create(
            Authentication authentication,
            TacheRequest request) {
        verifierPeriode(
                request.dateDebut(),
                request.dateFin());

        AppUser utilisateur = getUtilisateurConnecte(authentication);

        Tache tache = new Tache();

        tache.setUtilisateur(utilisateur);

        appliquerRequest(tache, request);

        Tache tacheEnregistree = tacheRepository.save(tache);

        return toResponse(tacheEnregistree);
    }

    public TacheResponse update(
            Authentication authentication,
            UUID id,
            TacheRequest request) {
        verifierPeriode(
                request.dateDebut(),
                request.dateFin());

        Tache tache = getTacheUtilisateur(
                authentication,
                id);

        appliquerRequest(tache, request);

        Tache tacheModifiee = tacheRepository.save(tache);

        return toResponse(tacheModifiee);
    }

    public void delete(
            Authentication authentication,
            UUID id) {
        Tache tache = getTacheUtilisateur(
                authentication,
                id);

        tacheRepository.delete(tache);
    }

    private void appliquerRequest(
            Tache tache,
            TacheRequest request) {
        Parametre typeTache = getParametre(
                request.typeTacheId(),
                "TYPE_TACHE");

        Parametre statut = getParametre(
                request.statutId(),
                "STATUT_TACHE");

        Parametre priorite = null;

        if (request.prioriteId() != null) {
            priorite = getParametre(
                    request.prioriteId(),
                    "PRIORITE_TACHE");
        }

        tache.setTypeTache(typeTache);
        tache.setStatut(statut);
        tache.setPriorite(priorite);
        tache.setTitre(request.titre().trim());
        tache.setDetails(request.details());
        tache.setDateDebut(request.dateDebut());
        tache.setDateFin(request.dateFin());
        tache.setTouteLaJournee(request.touteLaJournee());
    }

    private Parametre getParametre(
            Long id,
            String categorieAttendue) {
        Parametre parametre = parametreRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Le paramètre " + id + " est introuvable"));

        if (!parametre.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le paramètre sélectionné est désactivé");
        }

        if (!categorieAttendue.equals(parametre.getCategorie())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le paramètre sélectionné ne correspond pas à la catégorie "
                            + categorieAttendue);
        }

        return parametre;
    }

    private AppUser getUtilisateurConnecte(
            Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Utilisateur non connecté");
        }

        UUID identifiant;
        try {
            identifiant = UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return appUserRepository
                .findById(identifiant)
                .filter(AppUser::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Utilisateur connecté introuvable"));
    }

    private Tache getTacheUtilisateur(
            Authentication authentication,
            UUID id) {
        AppUser utilisateur = getUtilisateurConnecte(authentication);

        return tacheRepository
                .findByIdAndUtilisateurId(
                        id,
                        utilisateur.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tâche introuvable"));
    }

    private void verifierPeriode(
            OffsetDateTime dateDebut,
            OffsetDateTime dateFin) {
        if (dateDebut == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La date de début est obligatoire");
        }

        if (dateFin != null
                && dateFin.isBefore(dateDebut)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La date de fin doit être postérieure à la date de début");
        }
    }

    private TacheResponse toResponse(Tache tache) {
        return new TacheResponse(
                tache.getId(),
                tache.getTypeTache().getId(),
                tache.getStatut().getId(),
                tache.getPriorite() == null ? null : tache.getPriorite().getId(),
                tache.getTitre(),
                tache.getDetails(),
                tache.getDateDebut(),
                tache.getDateFin(),
                tache.isTouteLaJournee(),
                tache.getCreatedAt(),
                tache.getUpdatedAt());
    }

}
