package dev.nilptr.desafio.workers;

import dev.nilptr.desafio.dtos.RollbackInventoryUpdateDto;
import dev.nilptr.desafio.services.RollbackInventoryUpdateService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RollbackInventoryWorker {
    private final RollbackInventoryUpdateService rollbackInventoryUpdateService;

    @JobWorker(type = "rollbackUpdate", autoComplete = false)
    public void handleRollbackInventoryUpdate(JobClient client, ActivatedJob job, @VariablesAsType RollbackInventoryUpdateDto rollbackInventoryUpdateDto) {
        rollbackInventoryUpdateService.rollbackInventoryUpdate(rollbackInventoryUpdateDto).
                doOnSubscribe(sub -> log.info("Canceling update for " + rollbackInventoryUpdateDto.getAmount() + " of the item: " + rollbackInventoryUpdateDto.getProductId()))
                .flatMap(message -> {
                    log.info("Sending completion command with message: " + message);
                    return Mono.fromCompletionStage(
                            client.newCompleteCommand(job.getKey())
                                    .variables(Map.of("rollbackMessage", message))
                                    .send()
                    );
                })
                .doOnSuccess(success -> log.info("Successfully cancelled update for item: " + rollbackInventoryUpdateDto.getProductId()))
                .doOnError(throwable -> {
                    log.error("Failed to complete job: " + throwable.getMessage());
                    client.newFailCommand(job.getKey())
                            .retries(job.getRetries() - 1)
                            .errorMessage(throwable.getMessage())
                            .send()
                            .exceptionally(failThrowable -> {
                                log.error("Failed to fail job: " + failThrowable.getMessage());
                                return null;
                            });
                })
                .subscribe(
                        success -> log.info("Subscription success for compensation: " + rollbackInventoryUpdateDto.getProductId()),
                        error -> log.error("Subscription error: " + error.getMessage())
                );
    }
}
