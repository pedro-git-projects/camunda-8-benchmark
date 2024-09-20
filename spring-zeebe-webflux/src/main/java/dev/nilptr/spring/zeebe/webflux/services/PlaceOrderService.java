package dev.nilptr.spring.zeebe.webflux.services;

import dev.nilptr.spring.zeebe.webflux.dtos.PlaceOrderDto;
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
public class PlaceOrderService {
    private final ZeebeClient zeebeClient;

    public Mono<PublishMessageResponse> placeOrder(PlaceOrderDto placeOrderDto) {
        ZeebeFuture<PublishMessageResponse> future = zeebeClient.newPublishMessageCommand().messageName("orderPlaced").correlationKey(placeOrderDto.getOrderId()).variables(placeOrderDto.toVariableMap()).send();
        return Mono.create(sink -> future.whenComplete((publishMessageResponse, throwable) -> {
            if (throwable != null) {
                log.error("Failed with " + throwable);
                sink.error(throwable);
            } else {
                log.info("Successfully published message!");
                sink.success(publishMessageResponse);
            }
        }));
    }
}
