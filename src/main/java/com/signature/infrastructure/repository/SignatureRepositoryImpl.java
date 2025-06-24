package com.signature.infrastructure.repository;

import com.signature.domain.model.Signature;
import com.signature.domain.ports.SignatureRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SignatureRepositoryImpl implements SignatureRepository {
    private final SpringSignatureRepository repo;

    public SignatureRepositoryImpl(SpringSignatureRepository repo) {
        this.repo = repo;
    }

    @Override
    public Signature save(Signature signature) {
        return repo.save(signature);
    }

    @Override
    public List<Signature> findByUserId(Integer userId) {
        return repo.findByUserId(userId);
    }

    @Override
    public List<Signature> findByDocumentId(Integer documentId) {
        return repo.findByDocumentId(documentId);
    }
}
