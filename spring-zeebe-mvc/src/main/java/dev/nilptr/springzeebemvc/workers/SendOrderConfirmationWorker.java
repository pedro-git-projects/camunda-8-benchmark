package dev.nilptr.springzeebemvc.workers;

import dev.nilptr.springzeebemvc.dtos.UpdateInventoryDto;
import dev.nilptr.springzeebemvc.services.SendOrderConfirmationService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendOrderConfirmationWorker {

    private final SendOrderConfirmationService sendOrderConfirmationService;

    @JobWorker(type = "sendOrderConfirmation", autoComplete = false)
    public void handleSendOrderConfirmation(JobClient client, ActivatedJob job, @VariablesAsType UpdateInventoryDto updateInventoryDto) {
        sendOrderConfirmationService.sendOrderConfirmation(updateInventoryDto)
                .thenCompose(dto -> {
                    log.info("Messaging  " + dto.getCustomerEmail());
                    log.info("The new order status is " + dto.getOrderStatus());

                    return client.newCompleteCommand(job.getKey())
                            .variables(dto.toVariableMap())
                            .send()
                            .toCompletableFuture();
                })
                .thenAccept(response -> {
                    log.info("Successfully completed job for email: " + updateInventoryDto.getCustomerEmail());
                })
                .exceptionally(throwable -> {
                    log.error("Failed to complete job: " + throwable.getMessage());

                    client.newFailCommand(job.getKey())
                            .retries(job.getRetries() - 1)
                            .errorMessage(throwable.getMessage())
                            .send()
                            .exceptionally(failThrowable -> {
                                log.error("Failed to fail job: " + failThrowable.getMessage());
                                return null;
                            });
                    return null;
                });

        log.info("Sending confirmation email to: " + updateInventoryDto.getCustomerEmail());
    }
}

