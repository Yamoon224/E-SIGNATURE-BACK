package com.signature.domain.ports;

import com.signature.domain.model.Document;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository {
    Document save(Document document);
    Optional<Document> findById(Integer id);
    List<Document> findByOwnerId(Integer ownerId);
    void delete(Document document);
}