package backend.tache;

import backend.parametre.Parametre;
import backend.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tache", indexes = {
        @Index(name = "idx_tache_utilisateur_date", columnList = "utilisateur_id, date_debut")
})
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tache_utilisateur"))
    private AppUser utilisateur;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "type_tache_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tache_type"))
    private Parametre typeTache;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "statut_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tache_statut"))
    private Parametre statut;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "priorite_id", foreignKey = @ForeignKey(name = "fk_tache_priorite"))
    private Parametre priorite;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "date_debut", nullable = false)
    private OffsetDateTime dateDebut;

    @Column(name = "date_fin")
    private OffsetDateTime dateFin;

    @Column(name = "toute_la_journee", nullable = false)
    private boolean touteLaJournee = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Tache() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AppUser getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(AppUser utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Parametre getTypeTache() {
        return typeTache;
    }

    public void setTypeTache(Parametre typeTache) {
        this.typeTache = typeTache;
    }

    public Parametre getStatut() {
        return statut;
    }

    public void setStatut(Parametre statut) {
        this.statut = statut;
    }

    public Parametre getPriorite() {
        return priorite;
    }

    public void setPriorite(Parametre priorite) {
        this.priorite = priorite;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public OffsetDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(OffsetDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public OffsetDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(OffsetDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public boolean isTouteLaJournee() {
        return touteLaJournee;
    }

    public void setTouteLaJournee(boolean touteLaJournee) {
        this.touteLaJournee = touteLaJournee;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}