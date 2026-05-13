package com.pressing.gestion_pressing.repository;

import com.pressing.gestion_pressing.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {
    Page<Commande> findByEstRetireFalseAndEmailPretEnvoyeFalseAndDateRetraitPrevueBefore(LocalDateTime time, Pageable pageable);
    
    Page<Commande> findByEstRetireFalseAndDateRetraitPrevueAfter(LocalDateTime time, Pageable pageable);
    
    Page<Commande> findByUtilisateurEmail(String email, Pageable pageable);
}
