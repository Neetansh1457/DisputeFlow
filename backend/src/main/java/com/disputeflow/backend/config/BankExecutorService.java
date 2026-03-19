package com.disputeflow.backend.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.concurrent.*;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankExecutorService {

    private final ThreadPoolProperties properties;

    // One thread pool per bank — keyed by bank name
    private final Map<String, ThreadPoolExecutor> bankExecutors
            = new ConcurrentHashMap<>();

    /**
     * Submits a job to the thread pool for the given bank.
     * Creates a new pool if one doesn't exist yet for this bank.
     */
    public Future<?> submitJob(String bankName, Runnable task) {
        ThreadPoolExecutor executor = getOrCreateExecutor(bankName);
        log.info("Submitting job to {} pool — active: {}, queue: {}",
                bankName,
                executor.getActiveCount(),
                executor.getQueue().size());
        return executor.submit(task);
    }

    /**
     * Returns pool stats for a given bank.
     * Useful for monitoring and the manager dashboard.
     */
    public PoolStats getPoolStats(String bankName) {
        ThreadPoolExecutor executor = bankExecutors.get(bankName);
        if (executor == null) {
            return new PoolStats(bankName, 0, 0, 0, 0);
        }
        return new PoolStats(
                bankName,
                executor.getActiveCount(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount(),
                executor.getCorePoolSize()
        );
    }

    /**
     * Gets or creates a thread pool for the given bank.
     * ConcurrentHashMap ensures thread-safe creation.
     */
    private ThreadPoolExecutor getOrCreateExecutor(String bankName) {
        return bankExecutors.computeIfAbsent(bankName, name -> {
            log.info("Creating new thread pool for bank: {} (size: {})",
                    name, properties.getDefaultSize());

            return new ThreadPoolExecutor(
                    properties.getDefaultSize(),      // core pool size
                    properties.getMaxSize(),           // max pool size
                    60L,                               // keep alive time
                    TimeUnit.SECONDS,                  // time unit
                    new LinkedBlockingQueue<>(properties.getQueueCapacity()),
                    new BankThreadFactory(name),       // custom thread names
                    new ThreadPoolExecutor.CallerRunsPolicy() // if queue full, caller runs
            );
        });
    }

    /**
     * Gracefully shuts down all thread pools on app shutdown.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down {} bank thread pools", bankExecutors.size());
        bankExecutors.forEach((bankName, executor) -> {
            log.info("Shutting down pool for bank: {}", bankName);
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Custom thread factory that names threads by bank.
     * Makes logs much easier to read — you'll see which bank each thread belongs to.
     */
    private static class BankThreadFactory implements ThreadFactory {
        private final String bankName;
        private int threadCount = 0;

        BankThreadFactory(String bankName) {
            this.bankName = bankName;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(bankName + "-worker-" + (++threadCount));
            thread.setDaemon(false);
            return thread;
        }
    }

    /**
     * Simple stats object for monitoring.
     */
    public record PoolStats(
            String bankName,
            int activeThreads,
            int queuedJobs,
            long completedJobs,
            int poolSize
    ) {}
}