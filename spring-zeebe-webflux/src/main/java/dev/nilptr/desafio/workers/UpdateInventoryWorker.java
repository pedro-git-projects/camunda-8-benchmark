package dev.nilptr.desafio.workers;

import dev.nilptr.desafio.dtos.ConfirmStockDto;
import dev.nilptr.desafio.dtos.UpdateInventoryDto;
import dev.nilptr.desafio.services.UpdateInventoryService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateInventoryWorker {
    private final UpdateInventoryService updateInventoryService;

    @JobWorker(type = "updateInventory", autoComplete = false)
    public void handleUpdateInventory(JobClient client, ActivatedJob job, @VariablesAsType ConfirmStockDto confirmStockDto) {
        updateInventoryService.updateInventory(confirmStockDto)
                .doOnSubscribe(sub -> log.info("Updating inventory for order " + confirmStockDto.getOrderId() + "..."))
                .flatMap(dto -> {
                    return Mono.fromCompletionStage(client.newCompleteCommand(job.getKey())
                            .variables(dto.toVariableMap())
                            .send());
                }).doOnSuccess(success -> log.info("Successfully updated inventory for order " + confirmStockDto.getOrderId())).doOnError(throwable -> {
                    log.error("Failed to complete job: " + throwable.getMessage());
                    client.newFailCommand(job.getKey())
                            .retries(job.getRetries() - 1).errorMessage(throwable.getMessage())
                            .send().exceptionally(failThrowable -> {
                                log.error("Failed to fail job: " + failThrowable.getMessage());
                                return null;
                            });
                })
                .subscribe(
                        success -> log.info("Inventory update subscription success for order: " + confirmStockDto.getOrderId()),
                        error -> log.error("Inventory subscription error: " + error.getMessage())
                );
    }
}
