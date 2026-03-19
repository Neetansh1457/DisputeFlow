package com.disputeflow.backend.service;

import com.disputeflow.backend.dto.kafka.UploadJobEvent;
import com.disputeflow.backend.entity.UploadJob;
import com.disputeflow.backend.enums.JobStatus;
import com.disputeflow.backend.kafka.UploadJobProducer;
import com.disputeflow.backend.repository.UploadJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryService {

    private final UploadJobRepository uploadJobRepository;
    private final UploadJobProducer uploadJobProducer;
    private final AuditLogService auditLogService;

    /**
     * Runs every 5 minutes.
     * Finds all FAILED jobs that are eligible for retry
     * and republishes them to Kafka.
     */
    @Scheduled(fixedDelay = 300000) // every 5 minutes
    public void retryFailedJobs() {
        List<UploadJob> failedJobs = uploadJobRepository
                .findByStatusAndRetryCountLessThan(JobStatus.FAILED, 3);

        if (failedJobs.isEmpty()) return;

        log.info("Retry scheduler — found {} jobs eligible for retry", failedJobs.size());

        for (UploadJob job : failedJobs) {
            try {
                // Check if it's time to retry based on exponential backoff
                if (!isReadyForRetry(job)) continue;

                log.info("Retrying job {} — attempt {}", job.getId(), job.getRetryCount() + 1);

                job.setStatus(JobStatus.PENDING);
                job.setRetryCount(job.getRetryCount() + 1);
                job.setNextRetryAt(calculateNextRetry(job.getRetryCount()));
                uploadJobRepository.save(job);

                auditLogService.log(job, job.getUser(),
                        "RETRY_ATTEMPTED",
                        "Auto retry attempt " + job.getRetryCount() +
                                " of " + job.getMaxRetries());

                // Republish to Kafka
                UploadJobEvent event = UploadJobEvent.builder()
                        .jobId(job.getId())
                        .userId(job.getUser().getId())
                        .bankId(job.getBank().getId())
                        .bankName(job.getBank().getName())
                        .caseId(job.getCaseId())
                        .fileName(job.getFileName())
                        .filePath(job.getFilePath())
                        .fileHash(job.getFileHash())
                        .reasonCode(job.getReasonCode())
                        .documentType(job.getDocumentType())
                        .isBatch(job.getBatch() != null)
                        .batchId(job.getBatch() != null ? job.getBatch().getId() : null)
                        .build();

                uploadJobProducer.publishUploadJob(event);

            } catch (Exception e) {
                log.error("Error retrying job {}: {}", job.getId(), e.getMessage());
            }
        }
    }

    private boolean isReadyForRetry(UploadJob job) {
        if (job.getNextRetryAt() == null) return true;
        return LocalDateTime.now().isAfter(job.getNextRetryAt());
    }

    private LocalDateTime calculateNextRetry(int retryCount) {
        // Exponential backoff:
        // Retry 1 → wait 5 minutes
        // Retry 2 → wait 15 minutes
        // Retry 3 → wait 30 minutes
        int minutes = switch (retryCount) {
            case 1 -> 5;
            case 2 -> 15;
            default -> 30;
        };
        return LocalDateTime.now().plusMinutes(minutes);
    }
}