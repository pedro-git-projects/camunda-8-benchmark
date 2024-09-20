package dev.nilptr.spring.zeebe.webflux.workers;

import dev.nilptr.spring.zeebe.webflux.dtos.PlaceOrderDto;
import dev.nilptr.spring.zeebe.webflux.services.ProcessPaymentService;
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
public class ProcessPaymentWorker {
    private final ProcessPaymentService processPaymentService;

    @JobWorker(type = "processPayment", autoComplete = false)
    public void handleProcessPayment(JobClient client, ActivatedJob job, @VariablesAsType PlaceOrderDto placeOrderDto) {
        log.info("Received variables: " + placeOrderDto.toString());

        processPaymentService.processPayment(placeOrderDto)
                .doOnSubscribe(sub -> log.info("Starting payment processing for order: " + placeOrderDto.getOrderId()))
                .flatMap(dto -> {
                    log.info("Payment processed successfully, attempting to complete the job for order: " + placeOrderDto.getOrderId());
                    log.info("Sending completion command with variables: " + dto.toVariableMap());
                    return Mono.fromCompletionStage(
                            client.newCompleteCommand(job.getKey())
                                    .variables(dto.toVariableMap())
                                    .send()
                    );
                })
                .doOnSuccess(success -> log.info("Successfully completed job for order: " + placeOrderDto.getOrderId()))
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
                        success -> log.info("Subscription success for order: " + placeOrderDto.getOrderId()),
                        error -> log.error("Subscription error: " + error.getMessage())
                );
    }
}