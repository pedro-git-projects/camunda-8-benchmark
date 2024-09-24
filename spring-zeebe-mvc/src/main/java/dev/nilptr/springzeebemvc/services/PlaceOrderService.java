package dev.nilptr.springzeebemvc.services;

import dev.nilptr.springzeebemvc.dtos.PlaceOrderDto;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaceOrderService {
    private final ZeebeClient zeebeClient;

    public CompletableFuture<PublishMessageResponse> placeOrder(PlaceOrderDto placeOrderDto) {

        ZeebeFuture<PublishMessageResponse> future = zeebeClient.newPublishMessageCommand()
                .messageName("orderPlaced")
                .correlationKey(placeOrderDto.getOrderId())
                .variables(placeOrderDto.toVariableMap())
                .send();

        // Convert ZeebeFuture to CompletableFuture
        CompletableFuture<PublishMessageResponse> completableFuture = new CompletableFuture<>();

        future.whenComplete((publishMessageResponse, throwable) -> {
            if (throwable != null) {
                log.error("Failed with " + throwable);
                completableFuture.completeExceptionally(throwable);
            } else {
                log.info("Successfully published message!");
                completableFuture.complete(publishMessageResponse);
            }
        });

        return completableFuture;
    }
}
