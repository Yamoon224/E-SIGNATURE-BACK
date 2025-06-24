package com.signature.infrastructure.repository;

import com.signature.domain.model.Signature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringSignatureRepository extends JpaRepository<Signature, Integer> {
    List<Signature> findByUserId(Integer userId);
    List<Signature> findByDocumentId(Integer documentId);
}
