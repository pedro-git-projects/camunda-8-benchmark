package dev.nilptr.springzeebemvc.workers;

import dev.nilptr.springzeebemvc.dtos.RollbackInventoryUpdateDto;
import dev.nilptr.springzeebemvc.services.RollbackInventoryUpdateService;
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
public class RollbackInventoryWorker {

    private final RollbackInventoryUpdateService rollbackInventoryUpdateService;

    @JobWorker(type = "rollbackUpdate", autoComplete = false)
    public void handleRollbackInventoryUpdate(JobClient client, ActivatedJob job, @VariablesAsType RollbackInventoryUpdateDto rollbackInventoryUpdateDto) {
        rollbackInventoryUpdateService.rollbackInventoryUpdate(rollbackInventoryUpdateDto)
                .thenCompose(message -> {
                    log.info("Sending completion command with message: " + message);
                    return client.newCompleteCommand(job.getKey())
                            .variables(Map.of("rollbackMessage", message))
                            .send()
                            .toCompletableFuture();  // Convert ZeebeFuture to CompletableFuture
                })
                .thenAccept(response -> {
                    log.info("Successfully cancelled update for item: " + rollbackInventoryUpdateDto.getProductId());
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

        log.info("Canceling update for " + rollbackInventoryUpdateDto.getAmount() + " of the item: " + rollbackInventoryUpdateDto.getProductId());
    }
}