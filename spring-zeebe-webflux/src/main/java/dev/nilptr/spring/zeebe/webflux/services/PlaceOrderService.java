package dev.nilptr.spring.zeebe.webflux.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nilptr.spring.zeebe.webflux.dtos.PlaceOrderDto;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaceOrderService {
    private final ZeebeClient zeebeClient;

    private final int targetRequestCount = 10;
    private int requestCompletionCount = 0;
    private long startTime = 0;
    private final List<Map<String, Object>> throughputData = new LinkedList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<PublishMessageResponse> placeOrder(PlaceOrderDto placeOrderDto) {
        long requestStartTime = System.currentTimeMillis();

        if (requestCompletionCount == 0) {
            startTime = requestStartTime;
        }

        ZeebeFuture<PublishMessageResponse> future = zeebeClient.newPublishMessageCommand()
                .messageName("orderPlaced")
                .correlationKey(placeOrderDto.getOrderId())
                .variables(placeOrderDto.toVariableMap())
                .send();

        return Mono.create(sink -> future.whenComplete((publishMessageResponse, throwable) -> {
            if (throwable != null) {
                log.error("Failed to publish message: {}", throwable);
                sink.error(throwable);
            } else {
                log.info("Successfully published message for order: {}", placeOrderDto.getOrderId());
                sink.success(publishMessageResponse);

                requestCompletionCount++;

                if (requestCompletionCount >= targetRequestCount) {
                    long endTime = System.currentTimeMillis();
                    calculateThroughput(endTime);
                    requestCompletionCount = 0; // Reset for the next batch
                }
            }
        }));
    }

    private void calculateThroughput(long endTime) {
        long totalTimeMillis = endTime - startTime;
        double totalTimeSeconds = totalTimeMillis / 1000.0;

        double requestsPerSecond = targetRequestCount / totalTimeSeconds;
        log.info("Throughput: {} requests per second for the last {} requests", requestsPerSecond, targetRequestCount);

        Map<String, Object> throughputEntry = new HashMap<>();
        throughputEntry.put("timestamp", endTime);
        throughputEntry.put("throughput", requestsPerSecond);
        throughputData.add(throughputEntry);

        dumpThroughputDataToFile();
    }

    private void dumpThroughputDataToFile() {
        File file = new File("throughput-data-reactive-controller.json");

        List<Map<String, Object>> existingData = new LinkedList<>();
        if (file.exists()) {
            try {
                existingData = objectMapper.readValue(file, List.class);
            } catch (IOException e) {
                log.error("Failed to read existing throughput data from file", e);
            }
        }

        existingData.addAll(throughputData);

        try {
            objectMapper.writeValue(file, existingData);
            log.info("Throughput data appended to file: throughput-data-reactive-controller.json");
        } catch (IOException e) {
            log.error("Failed to write throughput data to file", e);
        }
        throughputData.clear();
    }
}