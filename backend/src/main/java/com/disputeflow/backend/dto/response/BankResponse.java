package com.disputeflow.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class BankResponse {
    private UUID id;
    private String name;
    private String filePrefix;
    private String caseIdField;
    private Boolean isActive;
    private Integer maxRetries;
    private Integer rateLimitPerMinute;
}