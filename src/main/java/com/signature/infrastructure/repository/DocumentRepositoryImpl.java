package com.signature.infrastructure.repository;

import com.signature.domain.model.Document;
import com.signature.domain.ports.DocumentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DocumentRepositoryImpl implements DocumentRepository {

    private final SpringDocumentRepository repo;

    public DocumentRepositoryImpl(SpringDocumentRepository repo) {
        this.repo = repo;
    }

    @Override
    public Document save(Document document) {
        return repo.save(document);
    }

    @Override
    public Optional<Document> findById(Integer id) {
        return repo.findById(id);
    }

    @Override
    public List<Document> findByOwnerId(Integer ownerId) {
        return repo.findByOwnerId(ownerId);
    }

    @Override
    public void delete(Document document) {
        repo.delete(document);
    }
}