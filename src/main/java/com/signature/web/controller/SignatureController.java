package com.signature.web.controller;

import com.signature.domain.model.Signature;
import com.signature.domain.model.Document;
import com.signature.domain.model.User;
import com.signature.domain.ports.SignatureRepository;
import com.signature.domain.ports.DocumentRepository;
import com.signature.domain.ports.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/signatures")
public class SignatureController {

    private final SignatureRepository signatureRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public SignatureController(SignatureRepository signatureRepository, DocumentRepository documentRepository, UserRepository userRepository) {
        this.signatureRepository = signatureRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Signature> signDocument(@RequestParam Integer documentId, @RequestParam Integer userId) {
        Signature signature = new Signature();
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        signature.setDocument(document);
        signature.setUser(user);
        signature.setSignedAt(LocalDateTime.now());

        Signature saved = signatureRepository.save(signature);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Signature>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(signatureRepository.findByUserId(userId));
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<Signature>> getByDocument(@PathVariable Integer documentId) {
        return ResponseEntity.ok(signatureRepository.findByDocumentId(documentId));
    }
}
