package dev.nilptr.springzeebemvc.workers;

import dev.nilptr.springzeebemvc.dtos.SendOrderConfirmationDto;
import dev.nilptr.springzeebemvc.services.CompleteOrderService;
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
public class CompleteOrderWorker {

    private final CompleteOrderService completeOrderService;

    @JobWorker(type = "completeOrder", autoComplete = false)
    public void handleCompleteOrder(JobClient client, ActivatedJob job, @VariablesAsType SendOrderConfirmationDto sendOrderConfirmationDto) {
        completeOrderService.completeOrder(sendOrderConfirmationDto)
                .thenCompose(dto -> {
                    log.info("Order completed successfully, attempting to complete the job for order: " + dto.getOrderId());
                    return client.newCompleteCommand(job.getKey())
                            .variables(dto.toVariableMap())
                            .send()
                            .toCompletableFuture();  // Convert ZeebeFuture to CompletableFuture
                })
                .thenAccept(response -> {
                    log.info("Successfully completed job for order: " + sendOrderConfirmationDto.getOrderId());
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

        log.info("Completing order: " + sendOrderConfirmationDto.getOrderId());
    }
}
