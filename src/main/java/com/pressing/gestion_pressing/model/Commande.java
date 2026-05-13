package com.pressing.gestion_pressing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "commandes")
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le type de service est obligatoire (NORMAL/RAPIDE/EXPRESS)")
    private String typeService;

    private Double prixTotal;

    private LocalDateTime dateCreation;
    private LocalDateTime dateRetraitPrevue;
    private LocalDateTime dateRetraitEffective;
    
    private boolean estRetire = false;
    private boolean emailPretEnvoye = false;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    @JsonBackReference
    private Utilisateur utilisateur;

    @NotEmpty(message = "La commande doit contenir au moins un vêtement")
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Vetement> vetements = new ArrayList<>();
}
