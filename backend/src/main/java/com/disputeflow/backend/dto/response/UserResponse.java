package com.disputeflow.backend.dto.response;

import com.disputeflow.backend.enums.UserRole;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;
}