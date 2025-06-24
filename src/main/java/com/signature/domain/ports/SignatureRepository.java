package com.signature.domain.ports;

import com.signature.domain.model.Signature;
import java.util.List;

public interface SignatureRepository {
    Signature save(Signature signature);
    List<Signature> findByUserId(Integer userId);
    List<Signature> findByDocumentId(Integer documentId);
}
