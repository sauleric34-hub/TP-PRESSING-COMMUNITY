package com.pressing.gestion_pressing.controller;

import com.pressing.gestion_pressing.dto.CommandeDTO;
import com.pressing.gestion_pressing.dto.UtilisateurDTO;
import com.pressing.gestion_pressing.model.PageResponse;
import java.util.List;
import java.util.Set;
import com.pressing.gestion_pressing.service.GestionPressingService;
import com.pressing.gestion_pressing.service.PdfService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
@RequestMapping("/web")
public class PressingWebController {

    @Autowired
    private GestionPressingService pressingService;

    private static final Logger logger = LoggerFactory.getLogger(PressingWebController.class);

    @Autowired
    private PdfService pdfService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ========== DASHBOARD ==========

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            Authentication authentication,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "5") int size,
                            @RequestParam(name = "recherche", required = false) String recherche) {
        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        logger.info("WEB: Accès au dashboard (User: {}, Admin: {}, page: {})", email, isAdmin, page);
        
        PageResponse<CommandeDTO> response;
        if (isAdmin) {
            response = pressingService.listerCommandes(page, size, recherche);
            model.addAttribute("utilisateurs", pressingService.listerTousLesUtilisateurs());
        } else {
            // Pour un utilisateur normal, on filtre ses commandes
            response = pressingService.listerCommandesParUtilisateur(email, page, size);
        }
        
        model.addAttribute("commandes", response);
        model.addAttribute("recherche", recherche);
        model.addAttribute("page", page);
        model.addAttribute("isAdmin", isAdmin);
        return "dashboard";
    }

    // ========== UTILISATEURS ==========

    @GetMapping("/utilisateurs")
    public String listeUtilisateurs(Model model,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "size", defaultValue = "10") int size,
                                    @RequestParam(name = "couleur", required = false) String couleur,
                                    @RequestParam(name = "categorie", required = false) String categorie) {
        logger.info("WEB: Liste des utilisateurs (Filtres: couleur={}, categorie={})", couleur, categorie);
        
        if (couleur != null && !couleur.isEmpty()) {
            List<UtilisateurDTO> list = pressingService.filtrerUtilisateursParCouleurVetement(couleur);
            model.addAttribute("utilisateurs", new PageResponse<>(list, 0, list.size(), (long) list.size(), 1));
        } else if (categorie != null && !categorie.isEmpty()) {
            List<UtilisateurDTO> list = pressingService.filtrerUtilisateursParCategorieVetement(categorie);
            model.addAttribute("utilisateurs", new PageResponse<>(list, 0, list.size(), (long) list.size(), 1));
        } else {
            model.addAttribute("utilisateurs", pressingService.listerUtilisateurs(page, size));
        }
        model.addAttribute("utilisateurForm", new UtilisateurDTO());
        model.addAttribute("page", page);
        model.addAttribute("couleur", couleur);
        model.addAttribute("categorie", categorie);
        return "utilisateurs";
    }

    @PostMapping("/utilisateurs")
    public String creerUtilisateur(@Valid @ModelAttribute("utilisateurForm") UtilisateurDTO dto,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        logger.info("WEB: Création utilisateur");
        if (result.hasErrors()) {
            model.addAttribute("utilisateurs", pressingService.listerUtilisateurs(0, 10));
            model.addAttribute("page", 0);
            return "utilisateurs";
        }
        try {
            pressingService.ajouterUtilisateur(dto);
            String successMsg = messageSource.getMessage("msg.user.created", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Une erreur inattendue est survenue : " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/web/utilisateurs";
    }

    @GetMapping("/utilisateurs/{id}/supprimer")
    public String supprimerUtilisateur(@PathVariable(name = "id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("WEB: Suppression utilisateur ID: {}", id);
        try {
            pressingService.supprimerUtilisateur(id);
            String successMsg = messageSource.getMessage("msg.user.deleted", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage("msg.error.delete", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }
        return "redirect:/web/utilisateurs";
    }

    // ========== COMMANDES ==========

    @GetMapping("/utilisateurs/{utilisateurId}/commander")
    public String formulaireCommande(@PathVariable(name = "utilisateurId") Long utilisateurId, Model model) {
        logger.info("WEB: Formulaire commande pour utilisateur ID: {}", utilisateurId);
        UtilisateurDTO utilisateur = pressingService.getUtilisateurById(utilisateurId);
        model.addAttribute("utilisateurId", utilisateurId);
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("commandeForm", new CommandeDTO());
        return "commande-form";
    }

    @PostMapping("/utilisateurs/{utilisateurId}/commander")
    public String creerCommande(@PathVariable(name = "utilisateurId") Long utilisateurId,
                                @Valid @ModelAttribute("commandeForm") CommandeDTO dto,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        logger.info("WEB: Création commande pour utilisateur ID: {}", utilisateurId);
        if (result.hasErrors()) {
            model.addAttribute("utilisateurId", utilisateurId);
            model.addAttribute("utilisateur", pressingService.getUtilisateurById(utilisateurId));
            return "commande-form";
        }
        try {
            pressingService.creerCommande(utilisateurId, dto);
            String successMsg = messageSource.getMessage("msg.order.created", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg;
            try {
                errorMsg = messageSource.getMessage("msg.error.general", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale());
            } catch (Exception ex) {
                errorMsg = "Error: " + e.getMessage();
            }
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }
        return "redirect:/web/dashboard";
    }

    @GetMapping("/commandes/{id}/facture")
    public ResponseEntity<byte[]> telechargerFacture(@PathVariable(name = "id") Long id, Authentication authentication) {
        logger.info("WEB: Téléchargement facture ID: {} par {}", id, authentication.getName());
        try {
            CommandeDTO dto = pressingService.getCommandeById(id);
            if (dto == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Sécurité : Un utilisateur normal ne peut télécharger que sa propre facture
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            if (!isAdmin && !dto.getNomUtilisateur().equals(authentication.getName()) && !dto.getTelephoneUtilisateur().equals(authentication.getName())) {
                // Note: On compare avec l'email ou le nom selon ce qui est stocké. 
                // Pour être sûr, on va comparer avec l'email de l'utilisateur de la commande
                UtilisateurDTO userDto = pressingService.getUtilisateurById(dto.getUtilisateurId());
                if (!userDto.getEmail().equals(authentication.getName())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
            byte[] pdf = pdfService.genererFacturePdf(dto);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facture_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            logger.error("ERREUR CRITIQUE FACTURE: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/commandes/retrait/{id}")
    public String marquerCommeRetire(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("WEB: Marquage retrait commande ID: {}", id);
        try {
            pressingService.marquerCommeRetire(id);
            String successMsg = messageSource.getMessage("msg.order.retired", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors du retrait : " + e.getMessage());
        }
        return "redirect:/web/dashboard";
    }

    // Redirection de la racine vers le dashboard
    @GetMapping("")
    public String root() {
        return "redirect:/web/dashboard";
    }
}
