package dev.nilptr.springzeebemvc.workers;

import dev.nilptr.springzeebemvc.dtos.PlaceOrderDto;
import dev.nilptr.springzeebemvc.services.ProcessPaymentService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProcessPaymentWorker {
    private final ProcessPaymentService processPaymentService;

    @JobWorker(type = "processPayment", autoComplete = false)
    public void handleProcessPayment(JobClient client, ActivatedJob job, @VariablesAsType PlaceOrderDto placeOrderDto) {
        log.info("Received variables: " + placeOrderDto.toString());

        processPaymentService.processPayment(placeOrderDto)
                .thenCompose(dto -> {
                    log.info("Payment processed successfully, attempting to complete the job for order: " + placeOrderDto.getOrderId());
                    log.info("Sending completion command with variables: " + dto.toVariableMap());

                    return client.newCompleteCommand(job.getKey())
                            .variables(dto.toVariableMap())
                            .send()
                            .toCompletableFuture();
                })
                .thenAccept(response -> {
                    log.info("Successfully completed job for order: " + placeOrderDto.getOrderId());
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

        log.info("Processing payment for order: " + placeOrderDto.getOrderId());
    }
}
