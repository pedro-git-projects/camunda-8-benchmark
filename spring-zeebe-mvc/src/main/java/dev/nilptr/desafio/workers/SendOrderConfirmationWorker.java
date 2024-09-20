package dev.nilptr.desafio.workers;

import dev.nilptr.desafio.dtos.UpdateInventoryDto;
import dev.nilptr.desafio.services.SendOrderConfirmationService;
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
public class SendOrderConfirmationWorker {
    private final SendOrderConfirmationService sendOrderConfirmationService;

    @JobWorker(type = "sendOrderConfirmation", autoComplete = false)
    public void handlesendOrderConfirmation(JobClient client, ActivatedJob job, @VariablesAsType UpdateInventoryDto updateInventoryDto) {
        sendOrderConfirmationService.sendOrderConfirmation(updateInventoryDto)
                .doOnSubscribe(sub -> log.info("Sending confirmation other to : " + updateInventoryDto.getCustomerEmail()))
                .flatMap(dto -> {
                    log.info("Messaging  " + dto.getCustomerEmail());
                    log.info("The new order status is " + dto.getOrderStatus());
                    return Mono.fromCompletionStage(
                            client.newCompleteCommand(job.getKey())
                                    .variables(dto.toVariableMap())
                                    .send()
                    );
                })
                .doOnSuccess(success -> log.info("Successfully completed email: " + updateInventoryDto.getCustomerEmail()))
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
                        success -> log.info("Subscription success for user: " + updateInventoryDto.getCustomerEmail()),
                        error -> log.error("Subscription error: " + error.getMessage())
                );
    }
}
