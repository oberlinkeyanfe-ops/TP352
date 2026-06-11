package com.banking.controller;

import com.banking.model.User;
import com.banking.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Authentification", description = "Creation de compte et connexion")
public class TokenController {
    
    private final TokenService tokenService;
    
    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }
    
    @PostMapping("/register")
    @Operation(summary = "S'inscrire", description = "Creer un nouveau compte utilisateur")
    public ResponseEntity<Map<String, Object>> register(
            @Parameter(description = "Nom complet", required = true, example = "Jean Dupont")
            @RequestParam String name,
            @Parameter(description = "Email unique", required = true, example = "jean@email.com")
            @RequestParam String email,
            @Parameter(description = "Numero de telephone", required = true, example = "0612345678")
            @RequestParam String phone) {
        
        // Validation supplementaire
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bad Request", "message", "Le nom est obligatoire"));
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bad Request", "message", "Format email invalide"));
        }
        
        try {
            User user = tokenService.createUserAndGetToken(name, email, phone);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", user.getToken());
            response.put("userId", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("message", "Compte créé avec succès");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null && (message.contains("Email") || message.contains("email"))) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Conflict", "message", message));
            }
            if (message != null && (message.contains("Telephone") || message.contains("phone"))) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Conflict", "message", message));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Bad Request", "message", message));
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Se connecter", description = "Obtenir un nouveau token")
    public ResponseEntity<Map<String, Object>> login(
            @Parameter(description = "Email", required = true, example = "jean@email.com")
            @RequestParam String email,
            @Parameter(description = "Telephone", required = true, example = "0612345678")
            @RequestParam String phone) {
        
        try {
            User user = tokenService.loginAndGetToken(email, phone);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", user.getToken());
            response.put("userId", user.getId());
            response.put("name", user.getName());
            response.put("message", "Connexion réussie");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unauthorized");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}