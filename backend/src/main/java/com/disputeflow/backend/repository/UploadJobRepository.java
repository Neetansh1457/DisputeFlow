package com.disputeflow.backend.repository;

import com.disputeflow.backend.entity.UploadJob;
import com.disputeflow.backend.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UploadJobRepository extends JpaRepository<UploadJob, UUID> {
    List<UploadJob> findByBatchId(UUID batchId);
    List<UploadJob> findByUserId(UUID userId);
    List<UploadJob> findByStatus(JobStatus status);
    List<UploadJob> findByBankId(UUID bankId);
    Optional<UploadJob> findByCaseIdAndBankId(String caseId, UUID bankId);
    boolean existsByCaseIdAndBankId(String caseId, UUID bankId);
    List<UploadJob> findByStatusAndRetryCountLessThan(JobStatus status, Integer maxRetries);
    List<UploadJob> findByUserIdOrderByCreatedAtDesc(UUID userId);
}