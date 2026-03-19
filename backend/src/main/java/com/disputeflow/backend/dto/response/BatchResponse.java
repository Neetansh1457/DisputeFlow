package com.disputeflow.backend.dto.response;

import com.disputeflow.backend.enums.BatchStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BatchResponse {
    private UUID id;
    private UUID userId;
    private Integer totalFiles;
    private Integer successful;
    private Integer failed;
    private Integer flagged;
    private BatchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<JobResponse> jobs;
}