package com.disputeflow.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class SingleUploadRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Bank ID is required")
    private UUID bankId;

    @NotBlank(message = "Case ID is required")
    private String caseId;

    private String documentType = "REPRESENTATION";
    private String reasonCode;
}