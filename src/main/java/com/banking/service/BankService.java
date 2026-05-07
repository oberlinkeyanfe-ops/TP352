package com.banking.service;

import com.banking.exception.ResourceNotFoundException;
import com.banking.model.Bank;
import com.banking.repository.BankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BankService {
    
    private final BankRepository bankRepository;
    
    public BankService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }
    
    @Transactional
    public Bank createBank(Bank bank) {
        if (bankRepository.existsByCode(bank.getCode())) {
            throw new RuntimeException("Code banque déjà utilisé: " + bank.getCode());
        }
        return bankRepository.save(bank);
    }
    
    public List<Bank> getAllBanks() {
        return bankRepository.findAll();
    }
    
    public Bank getBankById(Long id) {
        return bankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banque non trouvée avec id: " + id));
    }
    
    @Transactional
    public Bank updateBank(Long id, Bank bankDetails) {
        Bank bank = getBankById(id);
        bank.setName(bankDetails.getName());
        bank.setCode(bankDetails.getCode());
        return bankRepository.save(bank);
    }
    
    @Transactional
    public void deleteBank(Long id) {
        Bank bank = getBankById(id);
        bankRepository.delete(bank);
    }
}