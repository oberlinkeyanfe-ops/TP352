package com.banking.repository;

import com.banking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByToken(String token);
    Optional<User> findByEmailAndName(String email, String name);
    Optional<User> findByEmailAndPhone(String email, String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}