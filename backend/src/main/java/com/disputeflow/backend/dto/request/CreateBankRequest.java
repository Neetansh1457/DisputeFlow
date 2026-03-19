package com.disputeflow.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBankRequest {

    @NotBlank(message = "Bank name is required")
    private String name;

    @NotBlank(message = "File prefix is required")
    private String filePrefix;

    @NotBlank(message = "Case ID field is required")
    private String caseIdField;

    @NotBlank(message = "Mock endpoint is required")
    private String mockEndpoint;

    private Integer maxRetries = 3;
    private Integer retryIntervalMinutes = 5;
    private Integer timeoutSeconds = 30;
    private Integer rateLimitPerMinute = 60;
}