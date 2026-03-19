package com.disputeflow.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "banks")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String filePrefix;

    @Column(nullable = false)
    private String caseIdField;

    @Column(nullable = false)
    private String mockEndpoint;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Integer maxRetries = 3;
    private Integer retryIntervalMinutes = 5;
    private Integer timeoutSeconds = 30;
    private Integer rateLimitPerMinute = 60;

    @CreationTimestamp
    private LocalDateTime createdAt;
}