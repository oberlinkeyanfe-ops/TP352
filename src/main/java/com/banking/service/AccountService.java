package com.banking.service;

import com.banking.exception.ResourceNotFoundException;
import com.banking.model.Account;
import com.banking.model.Bank;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final BankService bankService;
    private final TokenService tokenService;
    
    public AccountService(AccountRepository accountRepository, 
                         BankService bankService, 
                         TokenService tokenService) {
        this.accountRepository = accountRepository;
        this.bankService = bankService;
        this.tokenService = tokenService;
    }
    
    @Transactional
    public Account createAccount(String token, Long bankId, String accountType) {
        if (accountType == null || accountType.trim().isEmpty()) {
            throw new RuntimeException("Le type de compte est obligatoire. Valeurs autorisees: CHECKING, SAVINGS");
        }
        
        if (!accountType.equals("CHECKING") && !accountType.equals("SAVINGS")) {
            throw new RuntimeException("Type de compte invalide. Valeurs autorisees: CHECKING, SAVINGS");
        }
        
        User user = tokenService.validateToken(token);
        Bank bank = bankService.getBankById(bankId);
        
        Account account = new Account();
        account.setAccountNumber("ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        account.setAccountType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setBank(bank);
        
        return accountRepository.save(account);
    }
    
    public List<Account> getUserAccounts(String token) {
        User user = tokenService.validateToken(token);
        return accountRepository.findByUserId(user.getId());
    }
    
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Compte non trouvé: " + accountNumber));
    }
    
    @Transactional
    public void deleteAccount(String token, Long accountId) {
        User user = tokenService.validateToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte non trouvé"));
        
        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Non autorisé à supprimer ce compte");
        }
        
        accountRepository.delete(account);
    }
    
    @Transactional
public Account deposit(String token, String accountNumber, BigDecimal amount) {
    User user = tokenService.validateToken(token);
    Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Compte non trouvé: " + accountNumber));
    
    if (!account.getUser().getId().equals(user.getId())) {
        throw new RuntimeException("Non autorisé à déposer sur ce compte");
    }
    
    // Modification ici : refuser montant <= 0
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new RuntimeException("Le montant doit être positif");
    }
    
    account.setBalance(account.getBalance().add(amount));
    return accountRepository.save(account);
}
}