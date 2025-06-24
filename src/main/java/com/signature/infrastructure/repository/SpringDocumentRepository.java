package com.signature.infrastructure.repository;

import com.signature.domain.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDocumentRepository extends JpaRepository<Document, Integer> {
    List<Document> findByOwnerId(Integer ownerId);
}