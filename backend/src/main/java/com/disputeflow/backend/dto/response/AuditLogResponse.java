package com.disputeflow.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AuditLogResponse {
    private UUID id;
    private UUID jobId;
    private String userName;
    private String action;
    private String details;
    private LocalDateTime createdAt;
}