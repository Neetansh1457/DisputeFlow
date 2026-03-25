package com.disputeflow.backend.service;

import com.disputeflow.backend.dto.kafka.UploadJobEvent;
import com.disputeflow.backend.dto.request.SingleUploadRequest;
import com.disputeflow.backend.dto.response.BatchPreviewResponse;
import com.disputeflow.backend.dto.response.JobResponse;
import com.disputeflow.backend.dto.response.BatchResponse;
import com.disputeflow.backend.entity.*;
import com.disputeflow.backend.enums.*;
import com.disputeflow.backend.kafka.UploadJobProducer;
import com.disputeflow.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private final UploadJobRepository uploadJobRepository;
    private final UploadBatchRepository uploadBatchRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final AuditLogService auditLogService;
    private final UploadJobProducer uploadJobProducer;
    private final UploadJobService uploadJobService;

    private static final String UPLOAD_DIR = "uploads/";

    // ================= SINGLE UPLOAD =================
    public JobResponse processSingleUpload(SingleUploadRequest request, MultipartFile file) {

        log.info("STEP 1: validate file");
        validateFile(file);

        log.info("STEP 2: duplicate check");
        if (uploadJobRepository.existsByCaseIdAndBankId(request.getCaseId(), request.getBankId())) {
            throw new RuntimeException("Case already submitted");
        }

        log.info("STEP 3: fetch user + bank");
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Bank bank = bankRepository.findById(request.getBankId())
                .orElseThrow(() -> new RuntimeException("Bank not found"));

        log.info("STEP 4: reading file bytes");
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file");
        }

        log.info("STEP 5: saving file");
        String filePath = saveFile(bytes, bank.getName(), request.getCaseId());

        log.info("STEP 6: skipping hash");
        String fileHash = "temp-hash";

        log.info("STEP 7: creating job");
        UploadJob job = UploadJob.builder()
                .user(user)
                .bank(bank)
                .caseId(request.getCaseId())
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileHash(fileHash)
                .documentType(request.getDocumentType())
                .reasonCode(request.getReasonCode())
                .status(JobStatus.PENDING)
                .retryCount(0)
                .maxRetries(bank.getMaxRetries())
                .autoProcessed(false)
                .build();

        UploadJob saved = uploadJobRepository.save(job);

        log.info("STEP 8: audit log");
        auditLogService.log(saved, user, "JOB_CREATED",
                "Single upload job created for bank: " + bank.getName());

        log.info("STEP 9: skipping Kafka");
        // uploadJobProducer.publishUploadJob(buildEvent(saved));

        log.info("STEP 10: returning response");
        return uploadJobService.mapToResponse(saved);
    }

    // ================= BATCH UPLOAD =================
    public BatchResponse processBatchUpload(UUID userId, List<MultipartFile> files) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UploadBatch batch = UploadBatch.builder()
                .user(user)
                .totalFiles(files.size())
                .successful(0)
                .failed(0)
                .flagged(0)
                .status(BatchStatus.PENDING)
                .build();

        UploadBatch savedBatch = uploadBatchRepository.save(batch);
        List<UploadJob> jobs = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                validateFile(file);

                FilenameParseResult parsed = parseFilename(file.getOriginalFilename());
                if (parsed.getBankId() == null) continue;

                Bank bank = bankRepository.findById(parsed.getBankId()).orElse(null);
                if (bank == null) continue;

                if (uploadJobRepository.existsByCaseIdAndBankId(parsed.getCaseId(), bank.getId())) {
                    continue;
                }

                byte[] bytes = file.getBytes();
                String filePath = saveFile(bytes, bank.getName(), parsed.getCaseId());

                UploadJob job = UploadJob.builder()
                        .batch(savedBatch)
                        .user(user)
                        .bank(bank)
                        .caseId(parsed.getCaseId())
                        .fileName(file.getOriginalFilename())
                        .filePath(filePath)
                        .fileHash("temp-hash")
                        .reasonCode(parsed.getReasonCode())
                        .documentType("REPRESENTATION")
                        .status(JobStatus.PENDING)
                        .retryCount(0)
                        .maxRetries(bank.getMaxRetries())
                        .autoProcessed(false)
                        .build();

                jobs.add(uploadJobRepository.save(job));

            } catch (Exception ex) {
                log.error("Error processing file {}: {}", file.getOriginalFilename(), ex.getMessage());
            }
        }

        savedBatch.setTotalFiles(jobs.size());
        savedBatch.setStatus(BatchStatus.PROCESSING);
        uploadBatchRepository.save(savedBatch);

        log.info("Publishing jobs skipped (Kafka disabled)");

        return BatchResponse.builder()
                .id(savedBatch.getId())
                .userId(userId)
                .totalFiles(savedBatch.getTotalFiles())
                .successful(0)
                .failed(0)
                .flagged(0)
                .status(BatchStatus.PROCESSING)
                .createdAt(savedBatch.getCreatedAt())
                .build();
    }

    // ================= HELPERS =================
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("Only PDF allowed");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("File too large");
        }
    }

    private String saveFile(byte[] bytes, String bankName, String caseId) {
        try {
            String dir = UPLOAD_DIR + bankName + "/";
            Files.createDirectories(Paths.get(dir));

            String fileName = caseId + "_" + System.currentTimeMillis() + ".pdf";
            Path path = Paths.get(dir + fileName);

            Files.write(path, bytes);

            return path.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
    }

    private FilenameParseResult parseFilename(String fileName) {
        FilenameParseResult result = new FilenameParseResult();
        if (fileName == null) return result;

        String name = fileName.replace(".pdf", "").replace(".PDF", "");
        String[] parts = name.split("_");

        if (parts.length < 2) return result;

        String prefix = parts[0].toUpperCase();
        result.setCaseId(parts[1]);
        if (parts.length >= 3) result.setReasonCode(parts[2]);

        bankRepository.findByFilePrefix(prefix).ifPresent(bank -> {
            result.setBankId(bank.getId());
            result.setBankName(bank.getName());
        });

        return result;
    }

    private UploadJobEvent buildEvent(UploadJob job) {
        return UploadJobEvent.builder()
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
    }

    @lombok.Data
    private static class FilenameParseResult {
        private UUID bankId;
        private String bankName;
        private String caseId;
        private String reasonCode;
    }
}