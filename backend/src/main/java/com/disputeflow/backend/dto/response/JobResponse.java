package com.disputeflow.backend.dto.response;

import com.disputeflow.backend.enums.ActionTaken;
import com.disputeflow.backend.enums.JobStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class JobResponse {
    private UUID id;
    private UUID batchId;
    private String bankName;
    private String caseId;
    private String fileName;
    private String documentType;
    private String reasonCode;
    private JobStatus status;
    private ActionTaken actionTaken;
    private Boolean autoProcessed;
    private String flaggedReason;
    private String failureReason;
    private String remarks;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}