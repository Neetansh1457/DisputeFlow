package com.disputeflow.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class BatchPreviewRequest {

    @NotEmpty(message = "At least one filename is required")
    private List<String> fileNames;

    @NotNull(message = "User ID is required")
    private UUID userId;
}