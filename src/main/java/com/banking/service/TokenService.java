package com.banking.service;

import com.banking.model.User;
import com.banking.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class TokenService {
    
    private final UserRepository userRepository;
    private final SecretKey secretKey = Keys.hmacShaKeyFor("maCleSecreteTresLonguePourBankingApp2024!".getBytes());
    private static final long EXPIRATION_TIME = 86400000; // 24 heures
    
    public TokenService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public String generateToken(Long userId, String email, String name) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .claim("name", name)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }
    
    @Transactional
    public User createUserAndGetToken(String name, String email, String phone) {
        // Vérifier si l'utilisateur existe déjà
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email déjà utilisé: " + email);
        }
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Téléphone déjà utilisé: " + phone);
        }
        
        // Créer un nouvel utilisateur
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user = userRepository.save(user);
        
        // Générer le token
        String token = generateToken(user.getId(), email, name);
        user.setToken(token);
        return userRepository.save(user);
    }
    
    @Transactional
    public User loginAndGetToken(String email, String phone) {
        User user = userRepository.findByEmailAndPhone(email, phone)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé. Vérifiez l'email et le téléphone"));
        
        // Générer un nouveau token
        String token = generateToken(user.getId(), user.getEmail(), user.getName());
        user.setToken(token);
        return userRepository.save(user);
    }
    
    public User validateToken(String token) {
        try {
            // Valider le token JWT
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Vérifier l'expiration
            if (claims.getExpiration().before(new Date())) {
                throw new RuntimeException("Token expiré");
            }
            
            // Chercher l'utilisateur avec ce token
            return userRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token invalide - utilisateur non trouvé"));
        } catch (Exception e) {
            throw new RuntimeException("Token invalide ou expiré: " + e.getMessage());
        }
    }
    
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'extraire l'ID du token");
        }
    }
}