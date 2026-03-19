package com.disputeflow.backend.kafka;

import com.disputeflow.backend.config.BankExecutorService;
import com.disputeflow.backend.dto.kafka.UploadJobEvent;
import com.disputeflow.backend.entity.Bank;
import com.disputeflow.backend.entity.UploadJob;
import com.disputeflow.backend.enums.ActionTaken;
import com.disputeflow.backend.enums.JobStatus;
import com.disputeflow.backend.repository.BankRepository;
import com.disputeflow.backend.repository.UploadJobRepository;
import com.disputeflow.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadJobConsumer {

    private final UploadJobRepository uploadJobRepository;
    private final BankRepository bankRepository;
    private final AuditLogService auditLogService;
    private final RestTemplate restTemplate;
    private final BankExecutorService bankExecutorService;

    private static final String PYTHON_ENGINE_URL = "http://localhost:8000";

    @KafkaListener(
            topics = "upload.jobs",
            groupId = "disputeflow-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(UploadJobEvent event) {
        log.info("Received job event — jobId: {}, bank: {}, caseId: {}",
                event.getJobId(), event.getBankName(), event.getCaseId());

        UploadJob job = uploadJobRepository.findById(event.getJobId()).orElse(null);
        if (job == null) {
            log.error("Job not found: {}", event.getJobId());
            return;
        }

        Bank bank = bankRepository.findById(event.getBankId()).orElse(null);
        if (bank == null) {
            log.error("Bank not found: {}", event.getBankId());
            return;
        }

        // Submit to bank-specific thread pool
        // This is the key change — each bank gets its own pool
        bankExecutorService.submitJob(bank.getName(), () -> processJob(job, bank, event));

        log.info("Job {} submitted to {} thread pool",
                event.getJobId(), bank.getName());

        // Log pool stats
        BankExecutorService.PoolStats stats =
                bankExecutorService.getPoolStats(bank.getName());
        log.info("Pool stats for {} — active: {}, queued: {}, completed: {}",
                bank.getName(),
                stats.activeThreads(),
                stats.queuedJobs(),
                stats.completedJobs());
    }

    private void processJob(UploadJob job, Bank bank, UploadJobEvent event) {
        log.info("Processing job {} on thread {}",
                job.getId(), Thread.currentThread().getName());
        try {
            job.setStatus(JobStatus.PROCESSING);
            uploadJobRepository.save(job);

            auditLogService.log(job, job.getUser(), "JOB_PROCESSING_STARTED",
                    "Processing on thread: " + Thread.currentThread().getName());

            Map<String, Object> result = callPythonEngine(job, bank);

            if (result == null) {
                handleFailure(job, "Python engine unavailable");
                return;
            }

            boolean success = (boolean) result.getOrDefault("success", false);
            String actionTaken = (String) result.getOrDefault("action_taken", "FAILED");
            String remarks = (String) result.getOrDefault("remarks", null);
            String errorMessage = (String) result.getOrDefault("error_message", null);
            boolean autoProcessed = (boolean) result.getOrDefault("auto_processed", false);

            job.setAutoProcessed(autoProcessed);
            job.setRemarks(remarks);

            if (success) {
                job.setStatus(JobStatus.SUCCESS);
                job.setActionTaken(ActionTaken.valueOf(actionTaken));
                auditLogService.log(job, job.getUser(), "JOB_COMPLETED",
                        "Action: " + actionTaken + " | " + remarks);
            } else {
                handleFailure(job, errorMessage);
            }

            uploadJobRepository.save(job);

        } catch (Exception ex) {
            log.error("Error processing job {}: {}", job.getId(), ex.getMessage());
            handleFailure(job, ex.getMessage());
        }
    }

    private Map<String, Object> callPythonEngine(UploadJob job, Bank bank) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("job_id", job.getId().toString());
            body.add("bank_prefix", bank.getFilePrefix());
            body.add("bank_name", bank.getName());
            body.add("mock_endpoint", bank.getMockEndpoint());
            body.add("case_id", job.getCaseId());
            if (job.getReasonCode() != null) {
                body.add("reason_code", job.getReasonCode());
            }
            body.add("timeout_seconds", bank.getTimeoutSeconds().toString());

            File file = new File(job.getFilePath());
            if (file.exists()) {
                body.add("file", new FileSystemResource(file));
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    PYTHON_ENGINE_URL + "/process",
                    requestEntity,
                    Map.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to call Python engine: {}", e.getMessage());
            return null;
        }
    }

    private void handleFailure(UploadJob job, String reason) {
        job.setStatus(JobStatus.FAILED);
        job.setFailureReason(reason);
        uploadJobRepository.save(job);
        auditLogService.log(job, job.getUser(), "JOB_FAILED", reason);
        log.error("Job {} failed: {}", job.getId(), reason);
    }
}
