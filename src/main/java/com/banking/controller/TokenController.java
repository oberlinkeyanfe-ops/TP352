package com.banking.controller;

import com.banking.model.User;
import com.banking.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "🔑 Authentification", description = "Création de compte et connexion")
public class TokenController {
    
    private final TokenService tokenService;
    
    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }
    
    @PostMapping("/register")
    @Operation(summary = "S'inscrire", description = "Créer un nouveau compte utilisateur")
    public ResponseEntity<Map<String, Object>> register(
            @Parameter(description = "Nom complet", required = true, example = "Jean Dupont")
            @RequestParam String name,
            
            @Parameter(description = "Email unique", required = true, example = "jean@email.com")
            @RequestParam String email,
            
            @Parameter(description = "Numéro de téléphone", required = true, example = "0612345678")
            @RequestParam String phone) {
        
        User user = tokenService.createUserAndGetToken(name, email, phone);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", user.getToken());
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("message", "Compte créé avec succès");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Se connecter", description = "Obtenir un nouveau token")
    public ResponseEntity<Map<String, Object>> login(
            @Parameter(description = "Email", required = true, example = "jean@email.com")
            @RequestParam String email,
            
            @Parameter(description = "Téléphone", required = true, example = "0612345678")
            @RequestParam String phone) {
        
        User user = tokenService.loginAndGetToken(email, phone);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", user.getToken());
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("message", "Connexion réussie");
        
        return ResponseEntity.ok(response);
    }
}