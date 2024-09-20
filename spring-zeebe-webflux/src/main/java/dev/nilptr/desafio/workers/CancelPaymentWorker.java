package dev.nilptr.desafio.workers;

import dev.nilptr.desafio.dtos.CancelPaymentDto;
import dev.nilptr.desafio.services.CancelPaymentService;
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
public class CancelPaymentWorker {

    private final CancelPaymentService cancelPaymentService;
    @JobWorker(type = "cancelPayment", autoComplete = false)
    public void handleCancelPayment(JobClient client, ActivatedJob job, @VariablesAsType CancelPaymentDto cancelPaymentDto) {
        cancelPaymentService.cancelPayment(cancelPaymentDto)
                .doOnSubscribe(sub -> log.info("Canceling payment" + cancelPaymentDto.getProcessId() +  "for order: " + cancelPaymentDto.getOrderId()))
                .flatMap(message -> {
                    log.info("Sending completion command with message: " + message);
                    return Mono.fromCompletionStage(
                            client.newCompleteCommand(job.getKey())
                                    .variables(Map.of("cancelMessage", message, "cancelled", true))
                                    .send()
                    );
                })
                .doOnSuccess(success -> log.info("Successfully cancelled order: " + cancelPaymentDto.getOrderId()))
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
                        success -> log.info("Subscription success for compensation: " + cancelPaymentDto.getProcessId()),
                        error -> log.error("Subscription error: " + error.getMessage())
                );
    }
}
