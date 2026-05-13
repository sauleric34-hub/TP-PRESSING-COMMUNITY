package com.pressing.gestion_pressing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandeDTO {
    private Long id;
    private Long utilisateurId;

    @NotBlank(message = "Le type de service est obligatoire (NORMAL/RAPIDE/EXPRESS)")
    private String typeService;

    private Double prixTotal;

    private LocalDateTime dateCreation;
    private LocalDateTime dateRetraitPrevue;
    private LocalDateTime dateRetraitEffective;
    
    private boolean estRetire;
    private boolean emailPretEnvoye;

    private String nomUtilisateur;
    private String telephoneUtilisateur;
    private String utilisateurEmail;

    @NotEmpty(message = "La commande doit contenir au moins un vêtement")
    @jakarta.validation.Valid
    private List<VetementDTO> vetements = new java.util.ArrayList<>();
}
