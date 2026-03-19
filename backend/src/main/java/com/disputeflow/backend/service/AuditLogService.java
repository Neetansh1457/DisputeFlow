package com.disputeflow.backend.service;

import com.disputeflow.backend.dto.response.AuditLogResponse;
import com.disputeflow.backend.entity.AuditLog;
import com.disputeflow.backend.entity.UploadJob;
import com.disputeflow.backend.entity.User;
import com.disputeflow.backend.repository.AuditLogRepository;
import com.disputeflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public void log(UploadJob job, User user, String action, String details) {
        AuditLog log = AuditLog.builder()
                .job(job)
                .user(user)
                .action(action)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> getLogsByJobId(UUID jobId) {
        return auditLogRepository.findByJobIdOrderByCreatedAtAsc(jobId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .jobId(log.getJob() != null ? log.getJob().getId() : null)
                .userName(log.getUser().getName())
                .action(log.getAction())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}