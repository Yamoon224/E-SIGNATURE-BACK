package com.signature.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signatures")
public class Signature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime signedAt = LocalDateTime.now();

    // Getters & Setters
    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public Document getDocument() {
        return document;
    }

    public User getUser() {
        return user;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
