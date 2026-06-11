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
        // Normaliser le telephone avant validation
        String normalizedPhone = normalizePhoneNumber(phone);
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email déjà utilisé: " + email);
        }
        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new RuntimeException("Téléphone déjà utilisé: " + normalizedPhone);
        }
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(normalizedPhone);
        user = userRepository.save(user);
        
        String token = generateToken(user.getId(), email, name);
        user.setToken(token);
        return userRepository.save(user);
    }

    private String normalizePhoneNumber(String phone) {
        if (phone == null) return null;
        
        // Si le numero commence par +33, le convertir en 0
        if (phone.startsWith("+33")) {
            return "0" + phone.substring(3);
        }
        
        return phone;
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

            if (token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7).trim();
            }

            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            if (claims.getExpiration().before(new Date())) {
                throw new RuntimeException("Token expiré");
            }
            
            return userRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token invalide - utilisateur non trouvé"));
        } catch (Exception e) {
            // Au lieu de lancer RuntimeException, lancer une exception specifique
            throw new SecurityException("Token invalide ou expiré: " + e.getMessage());
        }
    }
    
    public Long getUserIdFromToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("Le token est manquant");
            }
            if (token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7).trim();
            }
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            }
            if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            }
            return Long.parseLong(String.valueOf(userIdObj));
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'extraire l'ID du token");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("L'email est obligatoire");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            throw new RuntimeException("Format email invalide. Exemple: nom@domaine.com");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Le nom est obligatoire");
        }
        if (name.length() < 2) {
            throw new RuntimeException("Le nom doit contenir au moins 2 caracteres");
        }
    }

    private void validateAccountType(String accountType) {
        if (accountType == null || accountType.isBlank()) {
            throw new RuntimeException("Le type de compte est obligatoire");
        }
        if (!accountType.equals("CHECKING") && !accountType.equals("SAVINGS")) {
            throw new RuntimeException("Le type de compte doit etre CHECKING ou SAVINGS");
        }
    }
}