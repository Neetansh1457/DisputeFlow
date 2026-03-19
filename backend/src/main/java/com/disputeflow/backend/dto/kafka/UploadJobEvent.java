package com.disputeflow.backend.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadJobEvent {
    private UUID jobId;
    private UUID userId;
    private UUID bankId;
    private String bankName;
    private String caseId;
    private String fileName;
    private String filePath;
    private String fileHash;
    private String reasonCode;
    private String documentType;
    private boolean isBatch;
    private UUID batchId;
}
