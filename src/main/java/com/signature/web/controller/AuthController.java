package com.signature.web.controller;

import com.signature.domain.model.User;
import com.signature.domain.ports.UserRepository;
import com.signature.infrastructure.config.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "*") 
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

// Injection des dépendances via constructeur
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {        
        // 1. Recherche de l'utilisateur par email
        Optional<User> userOptional = userRepository.findByEmail(request.email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        User user = userOptional.get();

        // 2. Vérification du mot de passe
        if (!passwordEncoder.matches(request.password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        // 3. Génération du JWT
        String token = jwtService.generateToken(user.getEmail());
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", Map.of(
            "id", user.getId(),
            "email", user.getEmail()
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Vérifie si un utilisateur avec cet email existe déjà
        if (userRepository.findByEmail(request.email).isPresent()) {
            return ResponseEntity.badRequest().body("Email already taken");
        }
    
        User user = new User();
        user.setEmail(request.email);
        user.setPassword(passwordEncoder.encode(request.password));
    
        userRepository.save(user);
    
        return ResponseEntity.ok("User registered successfully");
    }

    // Classes statiques pour requêtes
    static class LoginRequest {
        public String email;
        public String password;
    }

    static class RegisterRequest {
        public String email;
        public String password;
    }
}
