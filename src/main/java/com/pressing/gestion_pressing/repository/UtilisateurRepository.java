package com.pressing.gestion_pressing.repository;

import com.pressing.gestion_pressing.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Concept de requête imbriquée avec EXISTS.
     * Trouve les utilisateurs ayant au moins un vêtement d'une couleur donnée.
     */
    @Query("SELECT u FROM Utilisateur u WHERE EXISTS (" +
           "SELECT 1 FROM Commande c JOIN c.vetements v " +
           "WHERE c.utilisateur = u AND LOWER(v.couleur) LIKE LOWER(CONCAT('%', :couleur, '%')))")
    List<Utilisateur> findUsersByGarmentColor(@Param("couleur") String couleur);

    /**
     * Concept de requête imbriquée avec IN.
     * Trouve les utilisateurs ayant au moins un vêtement d'une catégorie donnée.
     */
    @Query("SELECT u FROM Utilisateur u WHERE u.id IN (" +
           "SELECT c.utilisateur.id FROM Commande c JOIN c.vetements v " +
           "WHERE LOWER(v.categorie) = LOWER(:categorie))")
    List<Utilisateur> findUsersByGarmentCategory(@Param("categorie") String categorie);
}
