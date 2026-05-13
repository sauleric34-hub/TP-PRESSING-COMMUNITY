package com.pressing.gestion_pressing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VetementDTO {
    private Long id;

    @NotBlank(message = "Le nom du vêtement est obligatoire")
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
    
    private org.springframework.web.multipart.MultipartFile imageFile;
}
