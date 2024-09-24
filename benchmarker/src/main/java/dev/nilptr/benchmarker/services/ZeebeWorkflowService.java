package dev.nilptr.benchmarker.services;

import dev.nilptr.benchmarker.dtos.PlaceOrderDto;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZeebeWorkflowService {
    private final ZeebeClient zeebeClient;

    private static final String[] paymentMethods = {"CREDIT_CARD", "DEBIT_CARD", "APPLE_PAY", "GOOGLE_PAY"};

    private static final Random RANDOM = new Random();

    public CompletableFuture<Void> startWorkflowInstances(String processId, int numInstances) {

        return CompletableFuture.allOf(IntStream.range(0, numInstances)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    PlaceOrderDto orderDto = generateRandomOrder(i);
                    zeebeClient
                            .newPublishMessageCommand()
                            .messageName("orderPlaced")
                            .correlationKey(orderDto.getOrderId())
                            .variables(orderDto.toVariableMap())
                            .send()
                            .thenAccept(response -> {
                                log.info("Started workflow instance {} with variables: {}", i, orderDto);
                            })
                            .exceptionally(e -> {
                                log.error("Failed to start workflow instance {} with error: {}", i, e.getMessage());
                                return null;
                            });
                })).toArray(CompletableFuture[]::new));
    }

    private PlaceOrderDto generateRandomOrder(int instanceId) {
        PlaceOrderDto placeOrderDto = new PlaceOrderDto();
        placeOrderDto.setCustomerEmail("customer " + instanceId + "@example.com");
        placeOrderDto.setOrderId(Integer.toString(instanceId));
        placeOrderDto.setPaymentMethod(paymentMethods[RANDOM.nextInt(paymentMethods.length)]);
        placeOrderDto.setTotal(BigInteger.valueOf(RANDOM.nextInt(1000) + 100));
        placeOrderDto.setProductId(Integer.toString(RANDOM.nextInt(1000)));
        placeOrderDto.setAmount(RANDOM.nextInt(10) + 1);
        return placeOrderDto;
    }

}
