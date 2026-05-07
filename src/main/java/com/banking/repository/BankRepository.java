package com.banking.repository;

import com.banking.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional<Bank> findByCode(String code);
    Optional<Bank> findByName(String name);
    boolean existsByCode(String code);
}