package com.pressing.gestion_pressing.mapper;

import com.pressing.gestion_pressing.dto.CommandeDTO;
import com.pressing.gestion_pressing.dto.UtilisateurDTO;
import com.pressing.gestion_pressing.dto.VetementDTO;
import com.pressing.gestion_pressing.model.Commande;
import com.pressing.gestion_pressing.model.Utilisateur;
import com.pressing.gestion_pressing.model.Vetement;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.Set;
import com.pressing.gestion_pressing.model.Role;

@Component
public class PressingMapper {

    public UtilisateurDTO toUtilisateurDTO(Utilisateur utilisateur) {
        if (utilisateur == null) return null;
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setEmail(utilisateur.getEmail());
        dto.setTelephone(utilisateur.getTelephone());
        dto.setMotDePasse(null); // Ne jamais renvoyer le mot de passe dans le DTO pour des raisons de sécurité
        if (utilisateur.getRoles() != null) {
            dto.setRoles(utilisateur.getRoles().stream()
                    .map(Role::getNom)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    public Utilisateur toUtilisateurEntity(UtilisateurDTO dto) {
        if (dto == null) return null;
        Utilisateur u = new Utilisateur();
        u.setId(dto.getId());
        u.setNom(dto.getNom());
        u.setEmail(dto.getEmail());
        u.setTelephone(dto.getTelephone());
        u.setMotDePasse(dto.getMotDePasse());
        return u;
    }

    public CommandeDTO toCommandeDTO(Commande commande) {
        if (commande == null) return null;
        return new CommandeDTO(
                commande.getId(),
                commande.getUtilisateur() != null ? commande.getUtilisateur().getId() : null,
                commande.getTypeService(),
                commande.getPrixTotal(),
                commande.getDateCreation(),
                commande.getDateRetraitPrevue(),
                commande.getDateRetraitEffective(),
                commande.isEstRetire(),
                commande.isEmailPretEnvoye(),
                commande.getUtilisateur() != null ? commande.getUtilisateur().getNom() : null,
                commande.getUtilisateur() != null ? commande.getUtilisateur().getTelephone() : null,
                commande.getUtilisateur() != null ? commande.getUtilisateur().getEmail() : null,
                commande.getVetements() != null ? 
                    commande.getVetements().stream().map(this::toVetementDTO).collect(Collectors.toList()) : null
        );
    }

    public Commande toCommandeEntity(CommandeDTO dto) {
        if (dto == null) return null;
        Commande c = new Commande();
        c.setId(dto.getId());
        c.setTypeService(dto.getTypeService());
        c.setEstRetire(dto.isEstRetire());
        if (dto.getVetements() != null) {
            c.setVetements(dto.getVetements().stream().map(this::toVetementEntity).collect(Collectors.toList()));
        }
        return c;
    }

    public VetementDTO toVetementDTO(Vetement vetement) {
        if (vetement == null) return null;
        return new VetementDTO(
                vetement.getId(),
                vetement.getNom(),
                vetement.getCategorie(),
                vetement.getCouleur(),
                vetement.getDescription(),
                vetement.getPrixUnitaire(),
                vetement.getImageUrl(),
                null // imageFile
        );
    }

    public Vetement toVetementEntity(VetementDTO dto) {
        if (dto == null) return null;
        Vetement v = new Vetement();
        v.setId(dto.getId());
        v.setNom(dto.getNom());
        v.setCategorie(dto.getCategorie());
        v.setCouleur(dto.getCouleur());
        v.setDescription(dto.getDescription());
        v.setPrixUnitaire(dto.getPrixUnitaire());
        v.setImageUrl(dto.getImageUrl());
        return v;
    }
}
