package com.disputeflow.backend.repository;

import com.disputeflow.backend.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankRepository extends JpaRepository<Bank, UUID> {
    List<Bank> findByIsActiveTrue();
    Optional<Bank> findByFilePrefix(String filePrefix);
    Optional<Bank> findByName(String name);
}