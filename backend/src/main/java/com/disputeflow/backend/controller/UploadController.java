package com.disputeflow.backend.controller;

import com.disputeflow.backend.dto.request.BatchPreviewRequest;
import com.disputeflow.backend.dto.request.SingleUploadRequest;
import com.disputeflow.backend.dto.response.*;
import com.disputeflow.backend.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/single")
    public ResponseEntity<ApiResponse<JobResponse>> singleUpload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") @Valid SingleUploadRequest request) {
        JobResponse job = uploadService.processSingleUpload(request, file);
        return ResponseEntity.ok(ApiResponse.success("Upload job created", job));
    }

    @PostMapping("/batch/preview")
    public ResponseEntity<ApiResponse<BatchPreviewResponse>> previewBatch(
            @Valid @RequestBody BatchPreviewRequest request) {
        BatchPreviewResponse preview = uploadService.previewBatch(request.getFileNames());
        return ResponseEntity.ok(ApiResponse.success(preview));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<BatchResponse>> batchUpload(
            @RequestParam("userId") UUID userId,
            @RequestParam("files") List<MultipartFile> files) {
        BatchResponse batch = uploadService.processBatchUpload(userId, files);
        return ResponseEntity.ok(ApiResponse.success("Batch submitted successfully", batch));
    }
}