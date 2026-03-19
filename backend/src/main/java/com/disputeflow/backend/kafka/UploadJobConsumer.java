package com.disputeflow.backend.kafka;

import com.disputeflow.backend.dto.kafka.UploadJobEvent;
import com.disputeflow.backend.entity.UploadJob;
import com.disputeflow.backend.enums.JobStatus;
import com.disputeflow.backend.repository.UploadJobRepository;
import com.disputeflow.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadJobConsumer {

    private final UploadJobRepository uploadJobRepository;
    private final AuditLogService auditLogService;

    @KafkaListener(
            topics = "upload.jobs",
            groupId = "disputeflow-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(UploadJobEvent event) {
        log.info("Received job event — jobId: {}, bank: {}, caseId: {}",
                event.getJobId(), event.getBankName(), event.getCaseId());

        UploadJob job = uploadJobRepository.findById(event.getJobId())
                .orElse(null);

        if (job == null) {
            log.error("Job not found for event — jobId: {}", event.getJobId());
            return;
        }

        try {
            // Mark as processing
            job.setStatus(JobStatus.PROCESSING);
            uploadJobRepository.save(job);

            auditLogService.log(job, job.getUser(),
                    "JOB_PROCESSING_STARTED",
                    "Job picked up by Kafka consumer for bank: " + event.getBankName());

            // Processing logic will be added in Phase 5
            // For now we just mark it as processing
            log.info("Job {} is now PROCESSING — full processing logic coming in Phase 5",
                    event.getJobId());

        } catch (Exception ex) {
            log.error("Error processing job {} — {}", event.getJobId(), ex.getMessage());
            job.setStatus(JobStatus.FAILED);
            job.setFailureReason(ex.getMessage());
            uploadJobRepository.save(job);

            auditLogService.log(job, job.getUser(),
                    "JOB_FAILED",
                    "Job failed during processing: " + ex.getMessage());
        }
    }
}