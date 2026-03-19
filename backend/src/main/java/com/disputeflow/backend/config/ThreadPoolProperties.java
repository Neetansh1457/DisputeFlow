package com.disputeflow.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "disputeflow.threadpool")
public class ThreadPoolProperties {
    private int defaultSize = 5;
    private int maxSize = 10;
    private int queueCapacity = 25;
}