package com.pressing.gestion_pressing.service;



import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PdfService pdfService;

    @org.springframework.scheduling.annotation.Async
    public void envoyerEmailConfirmation(com.pressing.gestion_pressing.dto.CommandeDTO commande) {
        String destinataire = commande.getUtilisateurEmail() != null ? commande.getUtilisateurEmail() : "sauleric34@gmail.com";
        System.out.println("📧 [ASYNC] Tentative d'envoi d'email de confirmation à : " + destinataire);
        
        byte[] pdfFacture = pdfService.genererFacturePdf(commande);
        
        MimeMessage message = mailSender.createMimeMessage();
        
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("sauleric34@gmail.com");
            helper.setTo(destinataire);
            helper.setSubject("🧾 Facture #" + commande.getId() + " - Pressing Community");



            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String datePrevue = commande.getDateRetraitPrevue().format(formatter);

            // Construction du tableau des vêtements
            StringBuilder vetementsHtml = new StringBuilder();
            if (commande.getVetements() != null) {
                int index = 0;
                for (com.pressing.gestion_pressing.dto.VetementDTO v : commande.getVetements()) {
                    String cid = "vetement_" + index;
                    vetementsHtml.append("<tr>")
                        .append("<td style='padding:15px; border-bottom:1px solid #eee; width:110px;'>")
                        .append("<img src='cid:").append(cid).append("' width='100' height='100' style='border-radius:8px; object-fit:cover; border: 1px solid #ddd;'></td>")
                        .append("<td style='padding:15px; border-bottom:1px solid #eee; vertical-align: middle;'>")
                        .append("<b style='font-size: 1.2em; color: #333;'>").append(v.getNom()).append("</b><br>")
                        .append("<span style='color: #666; font-size: 1.1em;'>Couleur : ").append(v.getCouleur()).append("</span></td>")
                        .append("<td style='padding:15px; border-bottom:1px solid #eee; text-align:right; vertical-align: middle; font-weight: bold; font-size: 1.1em;'>")
                        .append(String.format("%.0f", v.getPrixUnitaire())).append(" FCFA</td>")
                        .append("</tr>");
                    index++;
                }
            }

            String htmlContent = "<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 10px; overflow: hidden;'>" +
                    "  <div style='background-color: #1a73e8; padding: 20px; text-align: center; color: white;'>" +
                    "    <img src='cid:logo' style='max-width: 100px; margin-bottom: 10px; border-radius: 50%;'>" +
                    "    <h1 style='margin: 0;'>Pressing Community</h1>" +
                    "  </div>" +
                    "  <div style='padding: 20px;'>" +
                    "    <h2 style='color: #1a73e8;'>Bonjour / Hello " + commande.getNomUtilisateur() + ",</h2>" +
                    "    <p>Votre commande a été enregistrée avec succès. / Your order has been successfully registered.</p>" +
                    "    <div style='background: #f8f9fa; padding: 15px; border-radius: 5px; margin-bottom: 20px;'>" +
                    "      <p><b>Numéro de commande / Order number :</b> #" + commande.getId() + "</p>" +
                    "      <p><b>Type de service / Service type :</b> " + commande.getTypeService() + "</p>" +
                    "      <p><b>Date de retrait prévue / Estimated pickup date :</b> <span style='color: #d93025; font-weight: bold;'>" + datePrevue + "</span></p>" +
                    "    </div>" +
                    "    <h3>Détails de vos articles / Item details :</h3>" +
                    "    <table style='width: 100%; border-collapse: collapse;'>" +
                    vetementsHtml.toString() +
                    "    </table>" +
                    "    <div style='text-align: right; margin-top: 20px; font-size: 1.2em;'>" +
                    "      <b>Total à régler / Total to pay : <span style='color: #1a73e8;'>" + String.format("%.0f", commande.getPrixTotal()) + " FCFA</span></b>" +
                    "    </div>" +
                    "  </div>" +
                    "  <div style='background-color: #f1f3f4; padding: 20px; text-align: center; font-size: 0.8em; color: #777;'>" +
                    "    <p>Pressing Community - Ouvert 7j/7 - Lavage & Pliage / Wash & Fold</p>" +
                    "    <p>Cameroun, Douala/Yaoundé</p>" +
                    "  </div>" +
                    "</div>" +
                    "</body></html>";

            helper.setText(htmlContent, true);

            // 2. AJOUT DES PIÈCES JOINTES (Facture PDF)
            helper.addAttachment("Facture_" + commande.getId() + ".pdf", new org.springframework.core.io.ByteArrayResource(pdfFacture));

            // 3. AJOUT DES IMAGES INLINE (Logo et Vêtements)
            // Ajout du logo
            File logoFile = new File("src/main/resources/static/image/logo-pressing.webp");
            if (logoFile.exists()) {
                helper.addInline("logo", new FileSystemResource(logoFile));
            } else {
                System.out.println("⚠️ Logo non trouvé : " + logoFile.getAbsolutePath());
            }

            // Ajout des images de vêtements
            if (commande.getVetements() != null) {
                int index = 0;
                String staticDir = "src/main/resources/static/image/";
                String uploadDir = "uploads/";
                for (com.pressing.gestion_pressing.dto.VetementDTO v : commande.getVetements()) {
                    if (v.getImageUrl() != null) {
                        File imgFile = null;
                        if (v.getImageUrl().contains("/image/")) {
                            String filename = v.getImageUrl().substring(v.getImageUrl().lastIndexOf("/") + 1);
                            imgFile = new File(staticDir + filename);
                        } else if (v.getImageUrl().contains("/uploads/")) {
                            String filename = v.getImageUrl().substring(v.getImageUrl().lastIndexOf("/") + 1);
                            imgFile = new File(uploadDir + filename);
                        }

                        if (imgFile != null && imgFile.exists()) {
                            helper.addInline("vetement_" + index, new FileSystemResource(imgFile));
                        } else if (imgFile != null) {
                            System.out.println("⚠️ Image non trouvée : " + imgFile.getAbsolutePath());
                        }
                    }
                    index++;
                }
            }

            mailSender.send(message);
            System.out.println("✅ Email HTML avec PDF envoyé avec succès à : " + destinataire);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @org.springframework.scheduling.annotation.Async("taskExecutor")
    public void envoyerEmailRappel(com.pressing.gestion_pressing.dto.CommandeDTO commande, java.time.Duration tempsRestant) {
        String destinataire = commande.getUtilisateurEmail() != null ? commande.getUtilisateurEmail() : "sauleric34@gmail.com";
        
        long jours = tempsRestant.toDays();
        long heures = tempsRestant.toHoursPart();
        long minutes = tempsRestant.toMinutesPart();
        long secondes = tempsRestant.toSecondsPart();

        String countdown = String.format("%d jours, %d heures, %d minutes et %d secondes", jours, heures, minutes, secondes);
        String countdownEn = String.format("%d days, %d hours, %d minutes and %d seconds", jours, heures, minutes, secondes);

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("sauleric34@gmail.com");
            helper.setTo(destinataire);
            helper.setSubject("⏰ Rappel : Votre commande #" + commande.getId() + " arrive bientôt !");

            String htmlContent = "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; border: 1px solid #6366f1; border-radius: 10px; overflow: hidden;'>" +
                    "  <div style='background-color: #6366f1; padding: 20px; text-align: center; color: white;'>" +
                    "    <h1 style='margin: 0;'>Pressing Community</h1>" +
                    "  </div>" +
                    "  <div style='padding: 20px; text-align: center;'>" +
                    "    <h2 style='color: #6366f1;'>Compte à rebours / Countdown</h2>" +
                    "    <p style='font-size: 1.2em;'>Plus que / Only :</p>" +
                    "    <div style='background: #f1f5f9; padding: 20px; border-radius: 10px; margin: 20px 0; font-weight: bold; font-size: 1.4em; color: #1e293b;'>" +
                    "      " + countdown + "<br>" +
                    "      <span style='font-size: 0.8em; color: #64748b; font-style: italic;'>" + countdownEn + "</span>" +
                    "    </div>" +
                    "    <p>avant le retrait de votre commande <b>#" + commande.getId() + "</b>.</p>" +
                    "    <p style='font-size: 0.9em; color: #64748b;'>Rappel envoyé toutes les 5 minutes pour démonstration.</p>" +
                    "  </div>" +
                    "</div>" +
                    "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("⏰ [RAPPEL] Email envoyé à : " + destinataire);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du rappel : " + e.getMessage());
        }
    }
}
