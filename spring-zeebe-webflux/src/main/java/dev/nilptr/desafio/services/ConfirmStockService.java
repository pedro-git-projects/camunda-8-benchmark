package dev.nilptr.desafio.services;

import dev.nilptr.desafio.dtos.ConfirmStockDto;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConfirmStockService {
    private final ZeebeClient zeebeClient;
    public Mono<PublishMessageResponse> confirmStock(ConfirmStockDto confirmStockDto) {
        confirmStockDto.setInStock(confirmStockDto.getAmount() % 2 == 0);

        ZeebeFuture<PublishMessageResponse> future =
                zeebeClient.newPublishMessageCommand()
                        .messageName("stockConfirmation").correlationKey(confirmStockDto.getOrderId()).variables(confirmStockDto.toVariableMap()).send();

        return Mono.create(sink -> future.whenComplete((publishMessageResponse, throwable) -> {
            if (throwable != null) {
                log.error("Failed with " + throwable);
                sink.error(throwable);
            } else {
                log.info("Successfully published stock confirmation message");
                sink.success(publishMessageResponse);
            }
        }));
    }

}
