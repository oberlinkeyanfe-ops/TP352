package com.banking.controller;

import com.banking.model.Account;
import com.banking.service.AccountService;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Comptes bancaires", description = "Gestion des comptes")
@SecurityRequirement(name = "Bearer")
public class AccountController {
    
    private final AccountService accountService;
    
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    
    @PostMapping
    public ResponseEntity<Account> createAccount(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam Long bankId,
            @RequestParam(defaultValue = "CHECKING") @Valid String accountType) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(accountService.createAccount(authorizationHeader, bankId, accountType));
    }
    
    @GetMapping
    @Operation(summary = "Mes comptes", description = "Lister tous mes comptes bancaires")
    public ResponseEntity<?> getMyAccounts(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        try {
            return ResponseEntity.ok(accountService.getUserAccounts(authorizationHeader));
        } catch (MalformedJwtException | ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse("Token invalide ou expiré"));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("token")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/{accountNumber}")
    @Operation(summary = "Détail d'un compte", description = "Obtenir les informations d'un compte")
    public ResponseEntity<?> getAccountByNumber(
            @Parameter(description = "Numéro de compte", required = true, example = "ACC-12345678")
            @PathVariable String accountNumber) {
        
        try {
            return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{accountId}")
    @Operation(summary = "Fermer un compte", description = "Supprimer définitivement un compte")
    public ResponseEntity<?> deleteAccount(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID du compte à supprimer", required = true, example = "1")
            @PathVariable Long accountId) {
        
        try {
            accountService.deleteAccount(authorizationHeader, accountId);
            return ResponseEntity.noContent().build();
        } catch (MalformedJwtException | ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse("Token invalide ou expiré"));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("token")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/deposit")
    @Operation(summary = "Faire un dépôt", description = "Déposer de l'argent sur un compte")
    public ResponseEntity<?> deposit(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "Numéro du compte", required = true, example = "ACC-12345678")
            @RequestParam String accountNumber,
            @Parameter(description = "Montant à déposer", required = true, example = "500.00")
            @RequestParam BigDecimal amount) {

        try {
            return ResponseEntity.ok(accountService.deposit(authorizationHeader, accountNumber, amount));
        } catch (MalformedJwtException | ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse("Token invalide ou expiré"));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("token")) {
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