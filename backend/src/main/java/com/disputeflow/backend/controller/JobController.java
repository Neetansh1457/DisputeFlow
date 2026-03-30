package com.disputeflow.backend.controller;

import com.disputeflow.backend.dto.response.ApiResponse;
import com.disputeflow.backend.dto.response.JobResponse;
import com.disputeflow.backend.enums.JobStatus;
import com.disputeflow.backend.service.UploadJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final UploadJobService uploadJobService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJob(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(uploadJobService.getJobById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobResponse>>> getJobs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) JobStatus status) {
        if (userId != null) {
            return ResponseEntity.ok(ApiResponse.success(uploadJobService.getJobsByUserId(userId)));
        }
        if (status != null) {
            return ResponseEntity.ok(ApiResponse.success(uploadJobService.getJobsByStatus(status)));
        }
        List<JobResponse> allJobs = uploadJobService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success(allJobs));
    }

    @PatchMapping("/{id}/retry")
    public ResponseEntity<ApiResponse<JobResponse>> retryJob(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Job queued for retry", uploadJobService.retryJob(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelJob(@PathVariable UUID id) {
        uploadJobService.cancelJob(id);
        return ResponseEntity.ok(ApiResponse.success("Job cancelled", null));
    }
}