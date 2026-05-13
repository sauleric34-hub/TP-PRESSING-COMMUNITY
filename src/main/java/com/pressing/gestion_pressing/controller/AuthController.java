package com.pressing.gestion_pressing.controller;

import com.pressing.gestion_pressing.dto.JwtResponse;
import com.pressing.gestion_pressing.dto.LoginRequest;
import com.pressing.gestion_pressing.dto.UtilisateurDTO;
import com.pressing.gestion_pressing.security.JwtUtils;
import com.pressing.gestion_pressing.service.GestionPressingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    GestionPressingService gestionPressingService;

    @Autowired
    JwtUtils jwtUtils;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @ModelAttribute LoginRequest loginRequest) {
        logger.info("Tentative de connexion pour l'utilisateur: {}", loginRequest.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        logger.info("Utilisateur connecté avec succès: {}", loginRequest.getEmail());

        return ResponseEntity.ok(new JwtResponse(jwt,
                null, // ID non disponible directement depuis UserDetails standard sans extension
                userDetails.getUsername(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @ModelAttribute UtilisateurDTO signUpRequest) {
        logger.info("Tentative d'inscription pour l'utilisateur: {}", signUpRequest.getEmail());
        UtilisateurDTO result = gestionPressingService.ajouterUtilisateur(signUpRequest);
        logger.info("Utilisateur inscrit avec succès: {}", signUpRequest.getEmail());
        return ResponseEntity.ok(result);
    }
}
