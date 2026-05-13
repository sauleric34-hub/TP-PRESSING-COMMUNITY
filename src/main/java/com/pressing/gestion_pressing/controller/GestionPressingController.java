package com.pressing.gestion_pressing.controller;

import com.pressing.gestion_pressing.dto.CommandeDTO;
import com.pressing.gestion_pressing.dto.UtilisateurDTO;
import com.pressing.gestion_pressing.model.PageResponse;
import com.pressing.gestion_pressing.service.GestionPressingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class GestionPressingController {

    @Autowired
    private GestionPressingService pressingService;

    private static final Logger logger = LoggerFactory.getLogger(GestionPressingController.class);

    @GetMapping("/utilisateurs")
    public ResponseEntity<Object> listerUtilisateurs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "couleur", required = false) String couleur,
            @RequestParam(name = "categorie", required = false) String categorie) {
        
        if (couleur != null && !couleur.isEmpty()) {
            logger.info("REST: Filtrage des utilisateurs par couleur: {}", couleur);
            return ResponseEntity.ok(pressingService.filtrerUtilisateursParCouleurVetement(couleur));
        }
        
        if (categorie != null && !categorie.isEmpty()) {
            logger.info("REST: Filtrage des utilisateurs par catégorie: {}", categorie);
            return ResponseEntity.ok(pressingService.filtrerUtilisateursParCategorieVetement(categorie));
        }

        logger.info("REST: Liste des utilisateurs paginée (page: {}, size: {})", page, size);
        return ResponseEntity.ok(pressingService.listerUtilisateurs(page, size));
    }

    @GetMapping("/utilisateurs/all")
    public ResponseEntity<List<UtilisateurDTO>> listerTousLesUtilisateurs() {
        logger.info("REST: Liste de tous les utilisateurs");
        return ResponseEntity.ok(pressingService.listerTousLesUtilisateurs());
    }

    @PostMapping("/utilisateurs")
    public ResponseEntity<UtilisateurDTO> creerUtilisateur(@Valid @RequestBody UtilisateurDTO utilisateurDTO) {
        logger.info("REST: Création d'un nouvel utilisateur: {}", utilisateurDTO.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(pressingService.ajouterUtilisateur(utilisateurDTO));
    }

    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<String> supprimerUtilisateur(@PathVariable("id") Long id) {
        logger.info("REST: Suppression de l'utilisateur ID: {}", id);
        pressingService.supprimerUtilisateur(id);
        return ResponseEntity.ok("Utilisateur et ses commandes associés supprimés avec succès.");
    }

    @GetMapping("/commandes")
    public ResponseEntity<PageResponse<CommandeDTO>> listerCommandes(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "recherche", required = false) String recherche) {
        logger.info("REST: Liste des commandes (recherche: '{}')", recherche);
        return ResponseEntity.ok(pressingService.listerCommandes(page, size, recherche));
    }

    @PostMapping("/utilisateurs/{utilisateurId}/commandes")
    public ResponseEntity<CommandeDTO> createCommande(
            @PathVariable("utilisateurId") Long utilisateurId,
            @Valid @ModelAttribute CommandeDTO commandeDTO) {
        logger.info("REST: Création d'une commande pour l'utilisateur ID: {}", utilisateurId);
        return ResponseEntity.status(HttpStatus.CREATED).body(pressingService.creerCommande(utilisateurId, commandeDTO));
    }

    @PostMapping("/commandes/{id}/retrait")
    public ResponseEntity<CommandeDTO> retirerCommande(@PathVariable("id") Long id) {
        logger.info("REST: Retrait de la commande ID: {}", id);
        return ResponseEntity.ok(pressingService.marquerCommeRetire(id));
    }
}
