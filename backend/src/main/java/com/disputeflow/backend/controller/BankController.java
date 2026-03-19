package com.disputeflow.backend.controller;

import com.disputeflow.backend.dto.request.CreateBankRequest;
import com.disputeflow.backend.dto.request.CreateReasonCodeRequest;
import com.disputeflow.backend.dto.response.ApiResponse;
import com.disputeflow.backend.dto.response.BankResponse;
import com.disputeflow.backend.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @PostMapping
    public ResponseEntity<ApiResponse<BankResponse>> createBank(
            @Valid @RequestBody CreateBankRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bank created", bankService.createBank(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BankResponse>>> getAllBanks() {
        return ResponseEntity.ok(ApiResponse.success(bankService.getAllActiveBanks()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BankResponse>> getBank(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bankService.getBankById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BankResponse>> updateBank(
            @PathVariable UUID id,
            @Valid @RequestBody CreateBankRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bank updated", bankService.updateBank(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateBank(@PathVariable UUID id) {
        bankService.deactivateBank(id);
        return ResponseEntity.ok(ApiResponse.success("Bank deactivated", null));
    }

    @PostMapping("/{id}/reason-codes")
    public ResponseEntity<ApiResponse<Void>> addReasonCode(
            @PathVariable UUID id,
            @Valid @RequestBody CreateReasonCodeRequest request) {
        bankService.addReasonCode(id, request);
        return ResponseEntity.ok(ApiResponse.success("Reason code added", null));
    }
}