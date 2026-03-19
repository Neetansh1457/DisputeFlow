package com.disputeflow.backend.kafka;

import com.disputeflow.backend.dto.kafka.UploadJobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadJobProducer {

    private static final String TOPIC = "upload.jobs";
    private final KafkaTemplate<String, UploadJobEvent> kafkaTemplate;

    public void publishUploadJob(UploadJobEvent event) {
        // Use bankId as partition key
        // Same bank always goes to same partition — maintains order per bank
        String partitionKey = event.getBankId().toString();

        CompletableFuture<SendResult<String, UploadJobEvent>> future =
                kafkaTemplate.send(TOPIC, partitionKey, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish job event for jobId: {} — {}",
                        event.getJobId(), ex.getMessage());
            } else {
                log.info("Published job event — jobId: {}, bank: {}, partition: {}",
                        event.getJobId(),
                        event.getBankName(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}