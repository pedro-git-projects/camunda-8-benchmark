package dev.nilptr.springzeebemvc.workers;

import dev.nilptr.springzeebemvc.dtos.ConfirmStockDto;
import dev.nilptr.springzeebemvc.services.UpdateInventoryService;
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
public class UpdateInventoryWorker {

    private final UpdateInventoryService updateInventoryService;

    @JobWorker(type = "updateInventory", autoComplete = false)
    public void handleUpdateInventory(JobClient client, ActivatedJob job, @VariablesAsType ConfirmStockDto confirmStockDto) {
        updateInventoryService.updateInventory(confirmStockDto)
                .thenCompose(dto -> {
                    log.info("Updating inventory for order " + confirmStockDto.getOrderId());
                    log.info("Inventory update with variables: " + dto.toVariableMap());

                    return client.newCompleteCommand(job.getKey())
                            .variables(dto.toVariableMap())
                            .send()
                            .toCompletableFuture(); // Convert ZeebeFuture to CompletableFuture
                })
                .thenAccept(response -> {
                    log.info("Successfully updated inventory for order " + confirmStockDto.getOrderId());
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

        log.info("Started updating inventory for order " + confirmStockDto.getOrderId());
    }
}