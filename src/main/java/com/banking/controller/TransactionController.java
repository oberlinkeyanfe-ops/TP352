package com.banking.controller;

import com.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "💸 Transactions", description = "Retraits et transferts d'argent")
@SecurityRequirement(name = "Bearer")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping("/withdraw")
    @Operation(summary = "Retrait vers un autre utilisateur", 
               description = "Envoyer de l'argent à un compte d'un autre utilisateur")
    public ResponseEntity<Map<String, String>> withdraw(
            HttpServletRequest request,
            @Parameter(description = "Votre numéro de compte source", required = true, example = "ACC-12345678")
            @RequestParam String sourceAccountNumber,
            @Parameter(description = "Numéro du compte destinataire", required = true, example = "ACC-87654321")
            @RequestParam String destinationAccountNumber,
            @Parameter(description = "Montant à retirer", required = true, example = "100.00")
            @RequestParam BigDecimal amount) {
        
        String token = request.getHeader("Authorization");
        transactionService.withdraw(token, sourceAccountNumber, destinationAccountNumber, amount);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Retrait de " + amount + " € effectué avec succès");
        response.put("from", sourceAccountNumber);
        response.put("to", destinationAccountNumber);
        response.put("amount", amount.toString() + " €");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/transfer")
    @Operation(summary = "Transfert entre mes comptes", 
               description = "Transférer de l'argent entre vos propres comptes")
    public ResponseEntity<Map<String, String>> transfer(
            HttpServletRequest request,
            @Parameter(description = "Votre numéro de compte source", required = true, example = "ACC-12345678")
            @RequestParam String sourceAccountNumber,
            @Parameter(description = "Votre numéro de compte destination", required = true, example = "ACC-11111111")
            @RequestParam String destinationAccountNumber,
            @Parameter(description = "Montant à transférer", required = true, example = "200.00")
            @RequestParam BigDecimal amount) {
        
        String token = request.getHeader("Authorization");
        transactionService.transfer(token, sourceAccountNumber, destinationAccountNumber, amount);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Transfert de " + amount + " € effectué avec succès");
        response.put("from", sourceAccountNumber);
        response.put("to", destinationAccountNumber);
        response.put("amount", amount.toString() + " €");
        
        return ResponseEntity.ok(response);
    }
}