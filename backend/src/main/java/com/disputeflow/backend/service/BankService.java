package com.disputeflow.backend.service;

import com.disputeflow.backend.dto.request.CreateBankRequest;
import com.disputeflow.backend.dto.request.CreateReasonCodeRequest;
import com.disputeflow.backend.dto.response.BankResponse;
import com.disputeflow.backend.entity.Bank;
import com.disputeflow.backend.entity.BankReasonCode;
import com.disputeflow.backend.repository.BankReasonCodeRepository;
import com.disputeflow.backend.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankRepository bankRepository;
    private final BankReasonCodeRepository reasonCodeRepository;

    public BankResponse createBank(CreateBankRequest request) {
        Bank bank = Bank.builder()
                .name(request.getName())
                .filePrefix(request.getFilePrefix())
                .caseIdField(request.getCaseIdField())
                .mockEndpoint(request.getMockEndpoint())
                .maxRetries(request.getMaxRetries())
                .retryIntervalMinutes(request.getRetryIntervalMinutes())
                .timeoutSeconds(request.getTimeoutSeconds())
                .rateLimitPerMinute(request.getRateLimitPerMinute())
                .isActive(true)
                .build();

        return mapToResponse(bankRepository.save(bank));
    }

    public List<BankResponse> getAllActiveBanks() {
        return bankRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BankResponse getBankById(UUID id) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank not found: " + id));
        return mapToResponse(bank);
    }

    public BankResponse updateBank(UUID id, CreateBankRequest request) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank not found: " + id));

        bank.setName(request.getName());
        bank.setFilePrefix(request.getFilePrefix());
        bank.setCaseIdField(request.getCaseIdField());
        bank.setMockEndpoint(request.getMockEndpoint());
        bank.setMaxRetries(request.getMaxRetries());
        bank.setRetryIntervalMinutes(request.getRetryIntervalMinutes());
        bank.setTimeoutSeconds(request.getTimeoutSeconds());
        bank.setRateLimitPerMinute(request.getRateLimitPerMinute());

        return mapToResponse(bankRepository.save(bank));
    }

    public void deactivateBank(UUID id) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank not found: " + id));
        bank.setIsActive(false);
        bankRepository.save(bank);
    }

    public void addReasonCode(UUID bankId, CreateReasonCodeRequest request) {
        Bank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new RuntimeException("Bank not found: " + bankId));

        BankReasonCode reasonCode = BankReasonCode.builder()
                .bank(bank)
                .reasonCode(request.getReasonCode())
                .action(request.getAction())
                .description(request.getDescription())
                .build();

        reasonCodeRepository.save(reasonCode);
    }

    private BankResponse mapToResponse(Bank bank) {
        return BankResponse.builder()
                .id(bank.getId())
                .name(bank.getName())
                .filePrefix(bank.getFilePrefix())
                .caseIdField(bank.getCaseIdField())
                .isActive(bank.getIsActive())
                .maxRetries(bank.getMaxRetries())
                .rateLimitPerMinute(bank.getRateLimitPerMinute())
                .build();
    }
}