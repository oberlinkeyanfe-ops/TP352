package com.banking.controller;

import com.banking.model.Bank;
import com.banking.service.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banks")
@Tag(name = "Banques", description = "Gestion des banques partenaires")
public class BankController {
    
    private final BankService bankService;
    
    public BankController(BankService bankService) {
        this.bankService = bankService;
    }
    
    @GetMapping
    @Operation(summary = "Liste des banques", description = "Obtenir toutes les banques disponibles")
    public ResponseEntity<List<Bank>> getAllBanks() {
        return ResponseEntity.ok(bankService.getAllBanks());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Detail d'une banque")
    public ResponseEntity<Bank> getBankById(
            @Parameter(description = "ID de la banque", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(bankService.getBankById(id));
    }
    
    @PostMapping
    @Operation(summary = "Ajouter une banque")
    public ResponseEntity<?> createBank(
            @Parameter(description = "Nom de la banque", required = true, example = "Banque Populaire")
            @RequestParam String name,
            
            @Parameter(description = "Code unique de la banque", required = true, example = "BP")
            @RequestParam String code) {
        
        // Validation des champs
        if (name == null || name.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", "Le nom de la banque est obligatoire");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (code == null || code.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", "Le code de la banque est obligatoire");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        Bank bank = new Bank();
        bank.setName(name);
        bank.setCode(code);
        return ResponseEntity.status(HttpStatus.CREATED).body(bankService.createBank(bank));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une banque")
    public ResponseEntity<Void> deleteBank(
            @Parameter(description = "ID de la banque a supprimer", required = true, example = "1")
            @PathVariable Long id) {
        bankService.deleteBank(id);
        return ResponseEntity.noContent().build();
    }
}