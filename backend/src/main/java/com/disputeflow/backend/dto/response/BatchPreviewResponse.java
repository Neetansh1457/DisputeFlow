package com.disputeflow.backend.dto.response;

import com.disputeflow.backend.enums.ReasonAction;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BatchPreviewResponse {
    private List<FilePreview> previews;
    private Integer totalFiles;
    private Integer readyCount;
    private Integer needsReviewCount;

    @Data
    @Builder
    public static class FilePreview {
        private String fileName;
        private String detectedBank;
        private String detectedCaseId;
        private String detectedReasonCode;
        private ReasonAction suggestedAction;
        private Boolean isReady;
        private String issueReason;
    }
}