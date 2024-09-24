package dev.nilptr.springzeebemvc.workers;

import dev.nilptr.springzeebemvc.dtos.CancelPaymentDto;
import dev.nilptr.springzeebemvc.services.CancelPaymentService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelPaymentWorker {

    private final CancelPaymentService cancelPaymentService;

    @JobWorker(type = "cancelPayment", autoComplete = false)
    public void handleCancelPayment(JobClient client, ActivatedJob job, @VariablesAsType CancelPaymentDto cancelPaymentDto) {
        // Call the async cancelPayment method from the service
        cancelPaymentService.cancelPaymentAsync(cancelPaymentDto)
                .thenCompose(message -> {
                    log.info("Sending completion command with message: " + message);
                    // Complete the job with a message and the "cancelled" variable set to true
                    return client.newCompleteCommand(job.getKey())
                            .variables(Map.of("cancelMessage", message, "cancelled", true))
                            .send()
                            .toCompletableFuture();  // Convert ZeebeFuture to CompletableFuture
                })
                .thenAccept(response -> log.info("Successfully cancelled order: " + cancelPaymentDto.getOrderId()))
                .exceptionally(throwable -> {
                    log.error("Failed to complete job: " + throwable.getMessage());
                    // Handle job failure
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
    }
}
