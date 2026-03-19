package com.disputeflow.backend.config;

import com.disputeflow.backend.entity.Bank;
import com.disputeflow.backend.entity.BankReasonCode;
import com.disputeflow.backend.entity.User;
import com.disputeflow.backend.enums.ReasonAction;
import com.disputeflow.backend.enums.UserRole;
import com.disputeflow.backend.repository.BankReasonCodeRepository;
import com.disputeflow.backend.repository.BankRepository;
import com.disputeflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final BankRepository bankRepository;
    private final BankReasonCodeRepository reasonCodeRepository;
    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            seedBanks();
            seedDefaultUser();
        };
    }

    private void seedBanks() {
        if (bankRepository.count() > 0) {
            log.info("Banks already seeded — skipping");
            return;
        }

        log.info("Seeding banks...");

        Bank amex = bankRepository.save(Bank.builder()
                .name("AMEX")
                .filePrefix("AMEX")
                .caseIdField("caseNumber")
                .mockEndpoint("http://localhost:8001/amex")
                .isActive(true)
                .maxRetries(3)
                .retryIntervalMinutes(5)
                .timeoutSeconds(30)
                .rateLimitPerMinute(60)
                .build());

        Bank hsbc = bankRepository.save(Bank.builder()
                .name("HSBC")
                .filePrefix("HSBC")
                .caseIdField("caseNumber")
                .mockEndpoint("http://localhost:8001/hsbc")
                .isActive(true)
                .maxRetries(3)
                .retryIntervalMinutes(5)
                .timeoutSeconds(30)
                .rateLimitPerMinute(60)
                .build());

        Bank pmtc = bankRepository.save(Bank.builder()
                .name("PMTC")
                .filePrefix("PMTC")
                .caseIdField("merchantOrderNumber")
                .mockEndpoint("http://localhost:8001/pmtc")
                .isActive(true)
                .maxRetries(3)
                .retryIntervalMinutes(5)
                .timeoutSeconds(30)
                .rateLimitPerMinute(60)
                .build());

        Bank chase = bankRepository.save(Bank.builder()
                .name("CHASE")
                .filePrefix("CHASE")
                .caseIdField("caseNumber")
                .mockEndpoint("http://localhost:8001/chase")
                .isActive(true)
                .maxRetries(3)
                .retryIntervalMinutes(5)
                .timeoutSeconds(30)
                .rateLimitPerMinute(60)
                .build());

        // AMEX reason codes
        reasonCodeRepository.save(BankReasonCode.builder()
                .bank(amex)
                .reasonCode("S01")
                .action(ReasonAction.ACCEPT)
                .description("Right of action — accept dispute, non representable scenario")
                .build());

        // PMTC reason codes
        reasonCodeRepository.save(BankReasonCode.builder()
                .bank(pmtc)
                .reasonCode("98")
                .action(ReasonAction.ACCEPT)
                .description("Fraud Reason Code")
                .build());

        log.info("Banks seeded successfully — AMEX, HSBC, PMTC, CHASE");
    }

    private void seedDefaultUser() {
        if (userRepository.existsByEmail("investigator@disputeflow.com")) {
            log.info("Default user already exists — skipping");
            return;
        }

        userRepository.save(User.builder()
                .name("Test Investigator")
                .email("investigator@disputeflow.com")
                .role(UserRole.INVESTIGATOR)
                .build());

        log.info("Default user seeded — investigator@disputeflow.com");
    }
}