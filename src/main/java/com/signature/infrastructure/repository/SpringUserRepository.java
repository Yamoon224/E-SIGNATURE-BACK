package com.signature.infrastructure.repository;

import com.signature.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringUserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}