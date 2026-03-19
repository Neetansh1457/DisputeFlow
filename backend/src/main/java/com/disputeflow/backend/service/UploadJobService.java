package com.disputeflow.backend.service;

import com.disputeflow.backend.dto.response.JobResponse;
import com.disputeflow.backend.entity.*;
import com.disputeflow.backend.enums.JobStatus;
import com.disputeflow.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UploadJobService {

    private final UploadJobRepository uploadJobRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final AuditLogService auditLogService;

    public JobResponse getJobById(UUID id) {
        UploadJob job = uploadJobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        return mapToResponse(job);
    }

    public List<JobResponse> getJobsByUserId(UUID userId) {
        return uploadJobRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<JobResponse> getJobsByStatus(JobStatus status) {
        return uploadJobRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public JobResponse retryJob(UUID jobId) {
        UploadJob job = uploadJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        if (job.getStatus() != JobStatus.FAILED) {
            throw new RuntimeException("Only FAILED jobs can be retried");
        }

        job.setStatus(JobStatus.PENDING);
        job.setRetryCount(job.getRetryCount() + 1);
        UploadJob saved = uploadJobRepository.save(job);

        auditLogService.log(saved, saved.getUser(), "RETRY_REQUESTED",
                "Manual retry requested by user");

        return mapToResponse(saved);
    }

    public void cancelJob(UUID jobId) {
        UploadJob job = uploadJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        if (job.getStatus() != JobStatus.PENDING) {
            throw new RuntimeException("Only PENDING jobs can be cancelled");
        }

        job.setStatus(JobStatus.SKIPPED);
        uploadJobRepository.save(job);

        auditLogService.log(job, job.getUser(), "JOB_CANCELLED",
                "Job cancelled by user");
    }

    public JobResponse mapToResponse(UploadJob job) {
        return JobResponse.builder()
                .id(job.getId())
                .batchId(job.getBatch() != null ? job.getBatch().getId() : null)
                .bankName(job.getBank().getName())
                .caseId(job.getCaseId())
                .fileName(job.getFileName())
                .documentType(job.getDocumentType())
                .reasonCode(job.getReasonCode())
                .status(job.getStatus())
                .actionTaken(job.getActionTaken())
                .autoProcessed(job.getAutoProcessed())
                .flaggedReason(job.getFlaggedReason())
                .failureReason(job.getFailureReason())
                .remarks(job.getRemarks())
                .retryCount(job.getRetryCount())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}