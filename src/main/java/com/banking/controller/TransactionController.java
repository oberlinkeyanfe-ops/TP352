package com.banking.controller;

import com.banking.service.TransactionService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Retraits et transferts d'argent")
@SecurityRequirement(name = "Bearer")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping("/withdraw")
    @Operation(summary = "Retrait vers un autre utilisateur", 
               description = "Envoyer de l'argent a un compte d'un autre utilisateur")
    public ResponseEntity<?> withdraw(
            HttpServletRequest request,
            @Parameter(description = "Votre numero de compte source", required = true, example = "ACC-12345678")
            @RequestParam String sourceAccountNumber,
            @Parameter(description = "Numero du compte destinataire", required = true, example = "ACC-87654321")
            @RequestParam String destinationAccountNumber,
            @Parameter(description = "Montant a retirer", required = true, example = "100.00")
            @RequestParam BigDecimal amount) {
        
        // Verification du token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse("Token manquant ou mal formaté"));
        }
        
        try {
            String token = authHeader.substring(7);
            transactionService.withdraw(token, sourceAccountNumber, destinationAccountNumber, amount);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Retrait de " + amount + " € effectue avec succes");
            response.put("from", sourceAccountNumber);
            response.put("to", destinationAccountNumber);
            response.put("amount", amount.toString() + " €");
            
            return ResponseEntity.ok(response);
            
        } catch (MalformedJwtException | ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse("Token invalide ou expiré"));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("token")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/transfer")
    @Operation(summary = "Transfert entre mes comptes", 
               description = "Transferer de l'argent entre vos propres comptes")
    public ResponseEntity<?> transfer(
            HttpServletRequest request,
            @Parameter(description = "Votre numero de compte source", required = true, example = "ACC-12345678")
            @RequestParam String sourceAccountNumber,
            @Parameter(description = "Votre numero de compte destination", required = true, example = "ACC-11111111")
            @RequestParam String destinationAccountNumber,
            @Parameter(description = "Montant a transférer", required = true, example = "200.00")
            @RequestParam BigDecimal amount) {
        
        // Verification du token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse("Token manquant ou mal formaté"));
        }
        
        try {
            String token = authHeader.substring(7);
            transactionService.transfer(token, sourceAccountNumber, destinationAccountNumber, amount);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Transfert de " + amount + " € effectue avec succes");
            response.put("from", sourceAccountNumber);
            response.put("to", destinationAccountNumber);
            response.put("amount", amount.toString() + " €");
            
            return ResponseEntity.ok(response);
            
        } catch (MalformedJwtException | ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse("Token invalide ou expiré"));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("token")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(e.getMessage()));
        }
    }
    
    private Map<String, String> errorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Unauthorized");
        error.put("message", message);
        return error;
    }
}