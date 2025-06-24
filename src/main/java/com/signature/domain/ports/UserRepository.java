package com.signature.domain.ports;

import com.signature.domain.model.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Integer id);
    Optional<User> findByEmail(String email);
    
    User save(User user);  // méthode pour sauvegarder un utilisateur
}
