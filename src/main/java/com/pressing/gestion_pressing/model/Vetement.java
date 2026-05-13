package com.pressing.gestion_pressing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vetements")
public class Vetement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du vêtement est obligatoire (ex: Chemise de luxe)")
    private String nom;

    @NotBlank(message = "La catégorie est obligatoire")
    private String categorie;
    
    @NotBlank(message = "La couleur est obligatoire")
    private String couleur;

    private String description;
    
    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Double prixUnitaire;

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "commande_id")
    @JsonBackReference
    private Commande commande;
}
