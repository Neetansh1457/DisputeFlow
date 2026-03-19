package com.disputeflow.backend.entity;

import com.disputeflow.backend.enums.ActionTaken;
import com.disputeflow.backend.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "upload_jobs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"case_id", "bank_id"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private UploadBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @Column(name = "case_id", nullable = false)
    private String caseId;

    private String fileName;
    private String filePath;
    private String fileHash;
    private String documentType;
    private String reasonCode;
    private String disputeStatus;

    @Enumerated(EnumType.STRING)
    private ActionTaken actionTaken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.PENDING;

    private Integer retryCount = 0;
    private Integer maxRetries = 3;
    private LocalDateTime nextRetryAt;

    private Boolean autoProcessed = false;
    private String flaggedReason;
    private String failureReason;
    private String remarks;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}