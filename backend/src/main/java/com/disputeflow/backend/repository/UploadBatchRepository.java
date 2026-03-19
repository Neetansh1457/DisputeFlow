package com.disputeflow.backend.repository;

import com.disputeflow.backend.entity.UploadBatch;
import com.disputeflow.backend.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface UploadBatchRepository extends JpaRepository<UploadBatch, UUID> {
    List<UploadBatch> findByUserId(UUID userId);
    List<UploadBatch> findByStatus(BatchStatus status);
    List<UploadBatch> findByUserIdOrderByCreatedAtDesc(UUID userId);
}