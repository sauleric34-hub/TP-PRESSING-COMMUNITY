package com.pressing.gestion_pressing.service;

import com.pressing.gestion_pressing.dto.CommandeDTO;
import com.pressing.gestion_pressing.dto.UtilisateurDTO;
import com.pressing.gestion_pressing.mapper.PressingMapper;
import com.pressing.gestion_pressing.model.Commande;
import com.pressing.gestion_pressing.model.PageResponse;
import com.pressing.gestion_pressing.model.Utilisateur;
import com.pressing.gestion_pressing.model.Vetement;
import com.pressing.gestion_pressing.repository.CommandeRepository;
import com.pressing.gestion_pressing.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GestionPressingService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private com.pressing.gestion_pressing.repository.RoleRepository roleRepository;

    @Autowired
    private PressingMapper mapper;

    @Autowired
    private TelephoneValidationService telephoneValidationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(GestionPressingService.class);



    public PageResponse<UtilisateurDTO> listerUtilisateurs(int page, int size) {
        Page<Utilisateur> utilisateurPage = utilisateurRepository.findAll(PageRequest.of(page, size));
        List<UtilisateurDTO> dtos = utilisateurPage.getContent().stream()
                .map(mapper::toUtilisateurDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                dtos,
                utilisateurPage.getNumber(),
                utilisateurPage.getSize(),
                utilisateurPage.getTotalElements(),
                utilisateurPage.getTotalPages()
        );
    }

    public List<UtilisateurDTO> listerTousLesUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(mapper::toUtilisateurDTO)
                .collect(Collectors.toList());
    }

    public List<UtilisateurDTO> filtrerUtilisateursParCouleurVetement(String couleur) {
        return utilisateurRepository.findUsersByGarmentColor(couleur).stream()
                .map(mapper::toUtilisateurDTO)
                .collect(Collectors.toList());
    }

    public List<UtilisateurDTO> filtrerUtilisateursParCategorieVetement(String categorie) {
        return utilisateurRepository.findUsersByGarmentCategory(categorie).stream()
                .map(mapper::toUtilisateurDTO)
                .collect(Collectors.toList());
    }

    public UtilisateurDTO getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .map(mapper::toUtilisateurDTO)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + id));
    }

    @Transactional
    public UtilisateurDTO ajouterUtilisateur(UtilisateurDTO dto) {
        logger.info("Service: Tentative d'ajout utilisateur: {} - Tel: {}", dto.getEmail(), dto.getTelephone());
        if (dto == null) return null;
        
        // Validation stricte Google LibPhoneNumber
        if (!telephoneValidationService.estValideCameroun(dto.getTelephone())) {
            logger.warn("Validation téléphone échouée pour: {}", dto.getTelephone());
            throw new IllegalArgumentException("Le numéro de téléphone n'est pas un numéro valide du Cameroun (+237 + 9 chiffres)");
        }
        
        Utilisateur utilisateur = mapper.toUtilisateurEntity(dto);
        // On reformate proprement au format E164 (+237...)
        utilisateur.setTelephone(telephoneValidationService.formaterInternational(utilisateur.getTelephone()));
        
        // Hachage du mot de passe
        if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        } else {
            utilisateur.setMotDePasse(passwordEncoder.encode("Password123!"));
        }

        // Attribution du rôle USER par défaut
        if (utilisateur.getRoles() == null || utilisateur.getRoles().isEmpty()) {
            roleRepository.findByNom("ROLE_USER").ifPresent(role -> utilisateur.setRoles(Set.of(role)));
        }

        logger.info("Ajout d'un nouvel utilisateur en base: {}", utilisateur.getEmail());
        
        return mapper.toUtilisateurDTO(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public void supprimerUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }

    public PageResponse<CommandeDTO> listerCommandes(int page, int size, String recherche) {
        Page<Commande> commandePage;
        if (recherche != null && !recherche.isEmpty()) {
            // Pour simplifier avec SQLite, on pourrait faire une recherche par type de service ou nom de vêtement
            // Ici on va faire simple : recherche par type de service
            // Dans un cas réel on utiliserait un Specification ou un Custom Query
            commandePage = commandeRepository.findAll(PageRequest.of(page, size)); // À améliorer si besoin
        } else {
            commandePage = commandeRepository.findAll(PageRequest.of(page, size));
        }

        List<CommandeDTO> dtos = commandePage.getContent().stream()
                .map(mapper::toCommandeDTO)
                .collect(Collectors.toList());

        // Filtrage manuel pour la démonstration si recherche présente
        if (recherche != null && !recherche.isEmpty()) {
            dtos = dtos.stream()
                .filter(c -> c.getTypeService().toLowerCase().contains(recherche.toLowerCase()))
                .collect(Collectors.toList());
        }

        return new PageResponse<>(
                dtos,
                commandePage.getNumber(),
                commandePage.getSize(),
                commandePage.getTotalElements(),
                commandePage.getTotalPages()
        );
    }

    public PageResponse<CommandeDTO> listerCommandesParUtilisateur(String email, int page, int size) {
        Page<Commande> commandePage = commandeRepository.findByUtilisateurEmail(email, PageRequest.of(page, size));
        List<CommandeDTO> dtos = commandePage.getContent().stream()
                .map(mapper::toCommandeDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                dtos,
                commandePage.getNumber(),
                commandePage.getSize(),
                commandePage.getTotalElements(),
                commandePage.getTotalPages()
        );
    }

    public CommandeDTO getCommandeById(Long id) {
        return commandeRepository.findById(id)
                .map(mapper::toCommandeDTO)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
    }

    @Transactional
    public CommandeDTO creerCommande(Long utilisateurId, CommandeDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + utilisateurId));

        Commande commande = mapper.toCommandeEntity(dto);
        LocalDateTime maintenant = LocalDateTime.now();
        commande.setDateCreation(maintenant);
        commande.setUtilisateur(utilisateur);
        
        double total = 0;
        if (commande.getVetements() != null) {
            for (Vetement v : commande.getVetements()) {
                v.setCommande(commande); // Liaison bidirectionnelle
                if (v.getPrixUnitaire() != null) {
                    total += v.getPrixUnitaire();
                }
                
                // Mapping des images locales selon la couleur (Seulement si non déjà renseigné par upload)
                if (v.getImageUrl() == null || v.getImageUrl().isEmpty()) {
                    String couleur = (v.getCouleur() != null) ? v.getCouleur().toLowerCase() : "";
                    if (couleur.contains("bleu")) {
                        v.setImageUrl("http://localhost:9999/image/tshirt_bleu.png");
                    } else if (couleur.contains("blanc")) {
                        v.setImageUrl("http://localhost:9999/image/tshirt_blanc.webp");
                    } else if (couleur.contains("gris")) {
                        v.setImageUrl("http://localhost:9999/image/tshirt_gris.webp");
                    } else {
                        v.setImageUrl("https://picsum.photos/seed/" + v.getNom().hashCode() + "/200");
                    }
                }
            }
            
            // Upload des images s'il y a des MultipartFiles attachés dans le DTO
            if (dto.getVetements() != null) {
                int index = 0;
                for (com.pressing.gestion_pressing.dto.VetementDTO vDto : dto.getVetements()) {
                    if (vDto.getImageFile() != null && !vDto.getImageFile().isEmpty()) {
                        try {
                            String fileName = java.util.UUID.randomUUID().toString() + "_" + vDto.getImageFile().getOriginalFilename();
                            java.nio.file.Path path = java.nio.file.Paths.get(System.getProperty("user.dir") + "/uploads/" + fileName);
                            java.nio.file.Files.createDirectories(path.getParent());
                            java.nio.file.Files.write(path, vDto.getImageFile().getBytes());
                            if (commande.getVetements().size() > index) {
                                commande.getVetements().get(index).setImageUrl("http://localhost:9999/uploads/" + fileName);
                            }
                        } catch (java.io.IOException e) {
                            System.err.println("Erreur upload image : " + e.getMessage());
                        }
                    }
                    index++;
                }
            }
        }
        
        // Logique de temps et de prix selon le service
        if ("EXPRESS".equalsIgnoreCase(commande.getTypeService())) {
            commande.setDateRetraitPrevue(maintenant.plusHours(8));
            total = total * 1.5; // Majoration 50% pour Express
        } else if ("RAPIDE".equalsIgnoreCase(commande.getTypeService())) {
            commande.setDateRetraitPrevue(maintenant.plusDays(4));
            total = total * 1.2; // Majoration 20% pour Rapide
        } else {
            commande.setDateRetraitPrevue(maintenant.plusDays(7));
            // Pas de majoration pour Normal
        }
        
        commande.setPrixTotal(total);
        Commande savedCommande = commandeRepository.save(commande);
        
        // On convertit en DTO pour retourner
        CommandeDTO savedDto = mapper.toCommandeDTO(savedCommande);
        
        // Envoi de l'email de confirmation (Asynchrone)
        emailService.envoyerEmailConfirmation(savedDto);
        
        return savedDto;
    }

    @Transactional
    public CommandeDTO marquerCommeRetire(Long idCommande) {
        Commande commande = commandeRepository.findById(idCommande)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
        
        commande.setEstRetire(true);
        commande.setDateRetraitEffective(LocalDateTime.now());
        return mapper.toCommandeDTO(commandeRepository.save(commande));
    }
}
