package com.pressing.gestion_pressing.config;

import com.pressing.gestion_pressing.model.Role;
import com.pressing.gestion_pressing.model.Utilisateur;
import com.pressing.gestion_pressing.repository.RoleRepository;
import com.pressing.gestion_pressing.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initialisation des données de base...");

        // Initialisation des rôles
        if (roleRepository.findByNom("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role("ROLE_USER"));
            logger.info("Rôle ROLE_USER créé.");
        }
        if (roleRepository.findByNom("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role("ROLE_ADMIN"));
            logger.info("Rôle ROLE_ADMIN créé.");
        }

        // Initialisation d'un admin par défaut
        if (utilisateurRepository.findByEmail("admin@pressing.com").isEmpty()) {
            Utilisateur admin = new Utilisateur();
            admin.setNom("Administrateur");
            admin.setEmail("admin@pressing.com");
            admin.setTelephone("+237600000000");
            admin.setMotDePasse(passwordEncoder.encode("Admin123!"));
            
            Role adminRole = roleRepository.findByNom("ROLE_ADMIN").get();
            admin.setRoles(Set.of(adminRole));
            
            utilisateurRepository.save(admin);
            logger.info("Utilisateur Admin par défaut créé: admin@pressing.com / Admin123!");
        }

        // Initialisation d'un utilisateur de test
        if (utilisateurRepository.findByEmail("user@pressing.com").isEmpty()) {
            Utilisateur user = new Utilisateur();
            user.setNom("Client Test");
            user.setEmail("user@pressing.com");
            user.setTelephone("+237611111111");
            user.setMotDePasse(passwordEncoder.encode("User123!"));
            
            Role userRole = roleRepository.findByNom("ROLE_USER").get();
            user.setRoles(Set.of(userRole));
            
            utilisateurRepository.save(user);
            logger.info("Utilisateur Client par défaut créé: user@pressing.com / User123!");
        }

        logger.info("Initialisation terminée.");
    }
}
