package dev.nilptr.springzeebemvc.services;

import dev.nilptr.springzeebemvc.dtos.ConfirmStockDto;
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
public class ConfirmStockService {

    private final ZeebeClient zeebeClient;

    public CompletableFuture<PublishMessageResponse> confirmStock(ConfirmStockDto confirmStockDto) {
        confirmStockDto.setInStock(confirmStockDto.getAmount() % 2 == 0);

        ZeebeFuture<PublishMessageResponse> future =
                zeebeClient.newPublishMessageCommand()
                        .messageName("stockConfirmation")
                        .correlationKey(confirmStockDto.getOrderId())
                        .variables(confirmStockDto.toVariableMap())
                        .send();

        // Convert ZeebeFuture to CompletableFuture
        CompletableFuture<PublishMessageResponse> completableFuture = new CompletableFuture<>();

        future.whenComplete((publishMessageResponse, throwable) -> {
            if (throwable != null) {
                log.error("Failed with " + throwable);
                completableFuture.completeExceptionally(throwable);
            } else {
                log.info("Successfully published stock confirmation message");
                completableFuture.complete(publishMessageResponse);
            }
        });

        return completableFuture;
    }
}
