package com.banking.controller;

import com.banking.model.User;
import com.banking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "👥 Utilisateurs", description = "Gestion des utilisateurs")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    @Operation(summary = "Liste des utilisateurs")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un utilisateur")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID de l'utilisateur", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @PostMapping
    @Operation(summary = "Créer un utilisateur")
    public ResponseEntity<User> createUser(
            @Parameter(description = "Nom complet", required = true, example = "Jean Dupont")
            @RequestParam String name,
            
            @Parameter(description = "Email unique", required = true, example = "jean@email.com")
            @RequestParam String email,
            
            @Parameter(description = "Numéro de téléphone", required = true, example = "0612345678")
            @RequestParam String phone) {
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Modifier un utilisateur")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID de l'utilisateur", required = true, example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "Nouveau nom", example = "Jean Martin")
            @RequestParam(required = false) String name,
            
            @Parameter(description = "Nouvel email", example = "jean.martin@email.com")
            @RequestParam(required = false) String email,
            
            @Parameter(description = "Nouveau téléphone", example = "0687654321")
            @RequestParam(required = false) String phone) {
        
        User userDetails = new User();
        if (name != null) userDetails.setName(name);
        if (email != null) userDetails.setEmail(email);
        if (phone != null) userDetails.setPhone(phone);
        
        return ResponseEntity.ok(userService.updateUser(id, userDetails));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID de l'utilisateur à supprimer", required = true, example = "1")
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}