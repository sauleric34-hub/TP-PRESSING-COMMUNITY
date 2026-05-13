package com.pressing.gestion_pressing.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

import java.util.UUID;

@Service
public class PdfService {

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] genererFacturePdf(com.pressing.gestion_pressing.dto.CommandeDTO commande) {
        Context context = new Context();
        
        // Conversion des images en Base64 pour le PDF (plus fiable que les URLs)
        if (commande.getVetements() != null) {
            for (com.pressing.gestion_pressing.dto.VetementDTO v : commande.getVetements()) {
                if (v.getImageUrl() != null && !v.getImageUrl().startsWith("data:")) {
                    v.setImageUrl(convertirImageEnBase64(v.getImageUrl()));
                }
            }
        }
        
        context.setVariable("commande", commande);
        context.setVariable("logoBase64", convertirImageEnBase64("http://localhost:9999/image/logo-pressing.webp"));
        
        // "Signature numérique" visuelle (Hash unique)
        String signatureHash = UUID.nameUUIDFromBytes((commande.getId() + commande.getNomUtilisateur()).getBytes()).toString().toUpperCase();
        context.setVariable("signatureHash", signatureHash);

        String html = templateEngine.process("facture", context);
        // System.out.println("DEBUG HTML: " + html); // Décommenter si besoin

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // Définition du chemin de base pour les images locales
            String baseUri = new java.io.File(".").toURI().toString();
            builder.withHtmlContent(html, baseUri);
            builder.toStream(os);
            builder.run();
            
            byte[] pdfBytes = os.toByteArray();
            
            // Signature numérique
            try (java.io.InputStream keystoreStream = new java.io.FileInputStream("src/main/resources/pressing.p12")) {
                return PdfSignatureUtil.signPdf(pdfBytes, keystoreStream, "password", "pressing");
            } catch (Exception e) {
                System.err.println("Erreur de signature, renvoi du PDF non signé : " + e.getMessage());
                return pdfBytes;
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
    private String convertirImageEnBase64(String urlOrPath) {
        try {
            byte[] imageBytes;
            String mimeType = "image/png";
            
            if (urlOrPath.contains("/image/")) {
                String filename = urlOrPath.substring(urlOrPath.lastIndexOf("/") + 1);
                java.io.File file = new java.io.File("src/main/resources/static/image/" + filename);
                if (file.exists()) {
                    if (filename.endsWith(".webp")) {
                        // openhtmltopdf ne supporte pas WEBP, on utilise Picsum comme fallback compatible
                        java.net.URL url = new java.net.URL("https://picsum.photos/seed/" + filename.hashCode() + "/200");
                        try (java.io.InputStream is = url.openStream()) {
                            imageBytes = is.readAllBytes();
                            mimeType = "image/jpeg";
                        }
                    } else {
                        imageBytes = java.nio.file.Files.readAllBytes(file.toPath());
                    }
                } else {
                    return urlOrPath;
                }
            } else if (urlOrPath.contains("/uploads/")) {
                String filename = urlOrPath.substring(urlOrPath.lastIndexOf("/") + 1);
                java.io.File file = new java.io.File("uploads/" + filename);
                if (file.exists()) {
                    imageBytes = java.nio.file.Files.readAllBytes(file.toPath());
                } else {
                    return urlOrPath;
                }
            } else if (urlOrPath.startsWith("http")) {
                // Pour Picsum par exemple
                java.net.URL url = new java.net.URL(urlOrPath);
                try (java.io.InputStream is = url.openStream()) {
                    imageBytes = is.readAllBytes();
                }
            } else {
                return urlOrPath;
            }
            
            String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
            return "data:" + mimeType + ";base64," + base64;
        } catch (Exception e) {
            System.err.println("Erreur conversion Base64 : " + e.getMessage());
            return urlOrPath;
        }
    }
}
