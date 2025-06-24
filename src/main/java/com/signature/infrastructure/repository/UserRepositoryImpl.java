package com.signature.infrastructure.repository;

import com.signature.domain.model.User;
import com.signature.domain.ports.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final SpringUserRepository repo;

    public UserRepositoryImpl(SpringUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return repo.save(user);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return repo.findById(id);
    }
}