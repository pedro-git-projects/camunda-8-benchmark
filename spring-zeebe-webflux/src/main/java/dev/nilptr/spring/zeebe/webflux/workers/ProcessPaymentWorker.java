package dev.nilptr.spring.zeebe.webflux.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nilptr.spring.zeebe.webflux.dtos.PlaceOrderDto;
import dev.nilptr.spring.zeebe.webflux.services.ProcessPaymentService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessPaymentWorker {
    private final ProcessPaymentService processPaymentService;

    private final int targetJobCount = 10;
    private int jobCompletionCount = 0;
    private long startTime = 0;
    private final List<Map<String, Object>> throughputData = new LinkedList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @JobWorker(type = "processPayment", autoComplete = false)
    public void handleProcessPayment(JobClient client, ActivatedJob job, @VariablesAsType PlaceOrderDto placeOrderDto) {
        long jobStartTime = System.currentTimeMillis();

        if (jobCompletionCount == 0) {
            startTime = jobStartTime;
        }

        log.info("Received variables: " + placeOrderDto.toString());

        processPaymentService.processPayment(placeOrderDto)
                .doOnSubscribe(sub -> log.info("Starting payment processing for order: " + placeOrderDto.getOrderId()))
                .flatMap(dto -> {
                    log.info("Payment processed successfully, attempting to complete the job for order: " + placeOrderDto.getOrderId());
                    log.info("Sending completion command with variables: " + dto.toVariableMap());
                    return Mono.fromCompletionStage(
                            client.newCompleteCommand(job.getKey())
                                    .variables(dto.toVariableMap())
                                    .send()
                    );
                })
                .doOnSuccess(success -> {
                            jobCompletionCount++;
                            log.info("Successfully completed job for order: " + placeOrderDto.getOrderId());

                            if (jobCompletionCount >= targetJobCount) {
                                long endTime = System.currentTimeMillis();
                                calculateThroughput(endTime);
                                jobCompletionCount = 0;
                            }
                        }
                )
                .doOnError(throwable -> {
                    log.error("Failed to complete job: " + throwable.getMessage());
                    client.newFailCommand(job.getKey())
                            .retries(job.getRetries() - 1)
                            .errorMessage(throwable.getMessage())
                            .send()
                            .exceptionally(failThrowable -> {
                                log.error("Failed to fail job: " + failThrowable.getMessage());
                                return null;
                            });
                })
                .subscribe(
                        success -> log.info("Subscription success for order: " + placeOrderDto.getOrderId()),
                        error -> log.error("Subscription error: " + error.getMessage())
                );
    }

    private void calculateThroughput(long endTime) {
        long totalTimeMillis = endTime - startTime;
        double totalTimeSeconds = totalTimeMillis / 1000.0;
        double jobsPerSecond = targetJobCount / totalTimeSeconds;

        log.info("Throughput: {} jobs per second for the last {} jobs", jobsPerSecond, targetJobCount);

        Map<String, Object> throughputEntry = new HashMap<>();
        throughputEntry.put("timestamp", endTime);
        throughputEntry.put("throughput", jobsPerSecond);
        throughputData.add(throughputEntry);

        dumpThroughputDataToFile();
    }

    private void dumpThroughputDataToFile() {
        File file = new File("throughput-data-batch.json");

        List<Map<String, Object>> existingData = new LinkedList<>();
        if (file.exists()) {
            try {
                existingData = objectMapper.readValue(file, List.class);
            } catch (IOException e) {
                log.error("Failed to read existing throughput data from file ", e);
            }
        }

        existingData.addAll(throughputData);

        try {
            objectMapper.writeValue(file, existingData);
            log.info("Throughput data appended to file: throughput-data-batch.json");
        } catch (IOException e) {
            log.error("Failed to write throughput data to file", e);
        }
        throughputData.clear();
    }
}