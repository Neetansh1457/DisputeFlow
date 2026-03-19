package com.disputeflow.backend.controller;

import com.disputeflow.backend.dto.response.ApiResponse;
import com.disputeflow.backend.dto.response.AuditLogResponse;
import com.disputeflow.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAllLogs() {
        return ResponseEntity.ok(ApiResponse.success(auditLogService.getAllLogs()));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getLogsByJob(
            @PathVariable UUID jobId) {
        return ResponseEntity.ok(ApiResponse.success(auditLogService.getLogsByJobId(jobId)));
    }
}