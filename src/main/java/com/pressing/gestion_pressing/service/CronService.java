package com.pressing.gestion_pressing.service;

import com.pressing.gestion_pressing.dto.CommandeDTO;
import com.pressing.gestion_pressing.mapper.PressingMapper;
import com.pressing.gestion_pressing.model.Commande;
import com.pressing.gestion_pressing.repository.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
public class CronService {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PressingMapper mapper;

    // 1. NOTIFICATION DE COMMANDE PRÊTE (Batch de 50)
    // @Scheduled(fixedRate = 60000) // Toutes les minutes (Désactivé pour préserver Gmail)
    @Transactional
    public void verifierCommandesPretes() {
        LocalDateTime maintenant = LocalDateTime.now();
        int page = 0;
        Page<Commande> commandesPretes;

        do {
            commandesPretes = commandeRepository.findByEstRetireFalseAndEmailPretEnvoyeFalseAndDateRetraitPrevueBefore(maintenant, PageRequest.of(page, 50));
            
            for (Commande commande : commandesPretes.getContent()) {
                try {
                    CommandeDTO dto = mapper.toCommandeDTO(commande);
                    emailService.envoyerEmailConfirmation(dto);
                    
                    commande.setEmailPretEnvoye(true);
                    commandeRepository.save(commande);
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors de l'envoi de l'email pour la commande #" + commande.getId() + " : " + e.getMessage());
                }
            }
            page++;
        } while (commandesPretes.hasNext());
    }

    // 2. RAPPEL TOUTES LES 2 MINUTES (Compte à rebours)
    // @Scheduled(fixedRate = 120000) // 2 minutes (Désactivé pour éviter les limites Gmail)
    @Transactional(readOnly = true)
    public void envoyerRappelsCountdown() {
        LocalDateTime maintenant = LocalDateTime.now();
        int page = 0;
        Page<Commande> commandesEnAttente;

        do {
            commandesEnAttente = commandeRepository.findByEstRetireFalseAndDateRetraitPrevueAfter(maintenant, PageRequest.of(page, 50));
            
            for (Commande commande : commandesEnAttente.getContent()) {
                try {
                    CommandeDTO dto = mapper.toCommandeDTO(commande);
                    Duration tempsRestant = Duration.between(maintenant, commande.getDateRetraitPrevue());
                    emailService.envoyerEmailRappel(dto, tempsRestant);
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors de l'envoi du rappel pour #" + commande.getId() + " : " + e.getMessage());
                }
            }
            page++;
        } while (commandesEnAttente.hasNext());
    }
}
