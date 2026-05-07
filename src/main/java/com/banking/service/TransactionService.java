package com.banking.service;

import com.banking.exception.ResourceNotFoundException;
import com.banking.model.Account;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class TransactionService {
    
    private final AccountRepository accountRepository;
    private final TokenService tokenService;
    
    public TransactionService(AccountRepository accountRepository, TokenService tokenService) {
        this.accountRepository = accountRepository;
        this.tokenService = tokenService;
    }
    
    @Transactional
    public void withdraw(String token, String sourceAccountNumber, 
                        String destinationAccountNumber, BigDecimal amount) {
        User user = tokenService.validateToken(token);
        
        Account sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Compte source non trouvé: " + sourceAccountNumber));
        
        Account destinationAccount = accountRepository.findByAccountNumber(destinationAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Compte destination non trouvé: " + destinationAccountNumber));
        
        // Vérifier que l'utilisateur est propriétaire du compte source
        if (!sourceAccount.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Non autorisé à retirer de ce compte source");
        }
        
        // Vérifier que le compte destination est différent
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new RuntimeException("Le compte source et destination doivent être différents");
        }
        
        // Vérifier le solde
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Solde insuffisant. Solde actuel: " + sourceAccount.getBalance());
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant doit être positif");
        }
        
        // Effectuer le transfert
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
        
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
    }
    
    @Transactional
    public void transfer(String token, String sourceAccountNumber, 
                        String destinationAccountNumber, BigDecimal amount) {
        User user = tokenService.validateToken(token);
        
        Account sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Compte source non trouvé: " + sourceAccountNumber));
        
        Account destinationAccount = accountRepository.findByAccountNumber(destinationAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Compte destination non trouvé: " + destinationAccountNumber));
        
        // Vérifier que l'utilisateur est propriétaire des DEUX comptes
        if (!sourceAccount.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Non autorisé à transférer depuis le compte source");
        }
        
        if (!destinationAccount.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Le compte destination doit vous appartenir pour un transfert");
        }
        
        // Vérifier le solde
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Solde insuffisant. Solde actuel: " + sourceAccount.getBalance());
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant doit être positif");
        }
        
        // Effectuer le transfert
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
        
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
    }
}