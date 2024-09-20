package dev.nilptr.desafio.workers;

import dev.nilptr.desafio.dtos.SendOrderConfirmationDto;
import dev.nilptr.desafio.services.CompleteOrderService;
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
public class CompleteOrderWorker {
    private final CompleteOrderService completeOrderService;

    @JobWorker(type = "completeOrder", autoComplete = false)
    public void handleCompleteOrder(JobClient client, ActivatedJob job, @VariablesAsType SendOrderConfirmationDto sendOrderConfirmationDto) {
        completeOrderService.completeOrder(sendOrderConfirmationDto)
                .doOnSubscribe(sub -> log.info("Completing order: " + sendOrderConfirmationDto.getOrderId()))
                .flatMap(dto -> {
                    log.info("Order completed successfully, attempting to complete the job for order: " + dto.getOrderId());
                    return Mono.fromCompletionStage(
                            client.newCompleteCommand(job.getKey())
                                    .variables(dto.toVariableMap())
                                    .send()
                    );
                })
                .doOnSuccess(success -> log.info("Successfully completed job for order: " + sendOrderConfirmationDto.getOrderId()))
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
                        success -> log.info("Subscription success for completion of order: " + sendOrderConfirmationDto.getOrderId()),
                        error -> log.error("Subscription error: " + error.getMessage())
                );
    }
}