package com.disputeflow.backend.entity;

import com.disputeflow.backend.enums.ReasonAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_reason_codes")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankReasonCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @Column(nullable = false)
    private String reasonCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReasonAction action;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;
}