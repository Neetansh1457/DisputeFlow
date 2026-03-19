package com.disputeflow.backend.repository;

import com.disputeflow.backend.entity.BankReasonCode;
import com.disputeflow.backend.enums.ReasonAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankReasonCodeRepository extends JpaRepository<BankReasonCode, UUID> {
    List<BankReasonCode> findByBankId(UUID bankId);
    Optional<BankReasonCode> findByBankIdAndReasonCode(UUID bankId, String reasonCode);
    List<BankReasonCode> findByBankIdAndAction(UUID bankId, ReasonAction action);
}