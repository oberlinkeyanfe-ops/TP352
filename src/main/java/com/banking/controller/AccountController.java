package com.banking.controller;

import com.banking.model.Account;
import com.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "💳 Comptes bancaires", description = "Gestion des comptes")
@SecurityRequirement(name = "Bearer")
public class AccountController {
    
    private final AccountService accountService;
    
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    
    @PostMapping
    @Operation(summary = "Ouvrir un compte", description = "Créer un nouveau compte bancaire")
    public ResponseEntity<Account> createAccount(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "ID de la banque", required = true, example = "1")
            @RequestParam Long bankId,
            @Parameter(description = "Type de compte (CHECKING, SAVINGS)", required = true, example = "CHECKING")
            @RequestParam(defaultValue = "CHECKING") String accountType) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(token, bankId, accountType));
    }
    
    @GetMapping
    @Operation(summary = "Mes comptes", description = "Lister tous mes comptes bancaires")
    public ResponseEntity<List<Account>> getMyAccounts(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(accountService.getUserAccounts(token));
    }
    
    @GetMapping("/{accountNumber}")
    @Operation(summary = "Détail d'un compte", description = "Obtenir les informations d'un compte")
    public ResponseEntity<Account> getAccountByNumber(
            @Parameter(description = "Numéro de compte", required = true, example = "ACC-12345678")
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }
    
    @DeleteMapping("/{accountId}")
    @Operation(summary = "Fermer un compte", description = "Supprimer définitivement un compte")
    public ResponseEntity<Void> deleteAccount(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "ID du compte à supprimer", required = true, example = "1")
            @PathVariable Long accountId) {
        accountService.deleteAccount(token, accountId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/deposit")
    @Operation(summary = "Faire un dépôt", description = "Déposer de l'argent sur un compte")
    public ResponseEntity<Account> deposit(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "Numéro du compte", required = true, example = "ACC-12345678")
            @RequestParam String accountNumber,
            @Parameter(description = "Montant à déposer", required = true, example = "500.00")
            @RequestParam BigDecimal amount) {
        
        return ResponseEntity.ok(accountService.deposit(token, accountNumber, amount));
    }
}