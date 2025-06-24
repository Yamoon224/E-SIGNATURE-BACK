package com.signature.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.signature.domain.model.Document;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import com.signature.domain.model.User;
import com.signature.domain.ports.UserRepository;
import com.signature.domain.ports.DocumentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.File;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import com.signature.util.ImageUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/documents")
public class DocumentController {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final SignatureController signatureController;

    public DocumentController(UserRepository userRepository, DocumentRepository documentRepository, SignatureController signatureController) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.signatureController = signatureController;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file selected to upload");
        }

        try {
            // 1. Récupérer l'email de l'utilisateur connecté depuis le SecurityContext
            String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
            // 2. Chercher l'utilisateur dans la base
            User owner = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
    
            // 3. Enregistrer le fichier sur disque
            String uploadDir = "assets/docs";
            Path uploadPath = Path.of(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
    
            Path filePath = uploadPath.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    
            // 4. Créer l'entité Document et la sauvegarder
            Document document = new Document();
            document.setFilename(file.getOriginalFilename());
            document.setPath(filePath.toString());
            document.setOwner(owner);
            document.setSigned(false);
            document.setCreatedAt(LocalDateTime.now());
    
            documentRepository.save(document);
    
            return ResponseEntity.ok("Document uploaded and saved with id: " + document.getId());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload document");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/sign")
    public ResponseEntity<?> signDocument(
        @PathVariable Integer id,
        @RequestParam(value = "signatureImage", required = false) MultipartFile signatureImage,
        @RequestParam("signatureText") String signatureText
    ) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        String pdfPath = document.getPath();


        try (PDDocument pdfDoc = PDDocument.load(new File(pdfPath))) {
            PDPage page = pdfDoc.getPage(0);
            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth = mediaBox.getWidth();
            float pageHeight = mediaBox.getHeight();

            float imageWidth = 90, imageHeight = 45;
            float marginRight = 5;
            float marginTop = 5;
            float x = pageWidth - imageWidth - marginRight;
            float y = pageHeight - imageHeight - marginTop;

            try (PDPageContentStream cs = new PDPageContentStream(pdfDoc, page, PDPageContentStream.AppendMode.APPEND, true)) {

                if (signatureImage != null && !signatureImage.isEmpty()) {
                    // 1. Lire directement les données du fichier avec removeBackground
                    // byte[] pngData = ImageUtils.removeBackground(signatureImage);
                    // PDImageXObject image = PDImageXObject.createFromByteArray(pdfDoc, pngData, "signature");
                    // 1. Définir le chemin du dossier
                    File outputDir = new File("assets/img");
                    if (!outputDir.exists()) {
                        outputDir.mkdirs(); // Crée le dossier s'il n'existe pas
                    }

                    // 2. Enregistrer l'image sous le nom "signature.png"
                    File imageFile = new File(outputDir, "signature.png");
                    signatureImage.transferTo(imageFile);

                    // 3. Charger l’image dans le PDF
                    PDImageXObject image = PDImageXObject.createFromFile(imageFile.getAbsolutePath(), pdfDoc);


                    // 2. Dessine l'image
                    cs.drawImage(image, x, y, imageWidth, imageHeight);
                    cs.beginText();
                    cs.setNonStrokingColor(0, 166, 125);
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    cs.newLineAtOffset(x, y - 12); // le texte juste sous l'image
                    cs.showText("Signed by: " + signatureText);
                    cs.endText();
                } else {
                    String text = "Approved by: " + signatureText;
                    float textX = pageWidth - 150;
                    float textY = pageHeight - 80;

                    cs.beginText();
                    cs.setNonStrokingColor(0, 166, 125);
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 12);

                    // Applique une rotation de 45° autour du point (textX, textY)
                    double angle = Math.toRadians(20);
                    float cos = (float) Math.cos(angle);
                    float sin = (float) Math.sin(angle);

                    // Transformation affine : rotation + translation
                    cs.setTextMatrix(cos, sin, -sin, cos, textX, textY);
                    cs.showText(text);
                    cs.endText();
                }
            }

            pdfDoc.save(pdfPath);
            document.setSigned(true);
            documentRepository.save(document);

            // Authentification
            String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User authenticatedUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            signatureController.signDocument(document.getId(), authenticatedUser.getId());
            
            return ResponseEntity.ok("Document signed" + (signatureImage != null && !signatureImage.isEmpty() ? " with image" : " with text only"));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error signing document: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable Integer id) {
        try {
            // Recherche du document
            Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document non trouvé"));

            // Authentification (optionnel si tu veux filtrer par utilisateur)
            String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User authenticatedUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouvé"));

            // Vérifie que l'utilisateur est bien le propriétaire du document (si pertinent)
            if (!document.getOwner().getId().equals(authenticatedUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès interdit à ce document");
            }

            // Retourne les infos du document
            return ResponseEntity.ok(document);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur: " + e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getDocumentsByUser() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        List<Document> documents = documentRepository.findByOwnerId(user.getId());
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}/destroy")
    public ResponseEntity<?> deleteDocument(@PathVariable Integer id) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        // Vérifier que l'utilisateur est bien le propriétaire
        if (!document.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous n'êtes pas autorisé à supprimer ce document.");
        }

        // Supprimer le fichier physique
        File file = new File(document.getPath());
        if (file.exists() && !file.delete()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la suppression du fichier.");
        }

        // Supprimer l'enregistrement en base
        documentRepository.delete(document);

        return ResponseEntity.ok("Document supprimé avec succès.");
    }
}