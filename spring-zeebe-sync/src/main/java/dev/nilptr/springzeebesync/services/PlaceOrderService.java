package dev.nilptr.springzeebesync.services;

import dev.nilptr.springzeebesync.dtos.PlaceOrderDto;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaceOrderService {
    private final ZeebeClient zeebeClient;

    public PublishMessageResponse placeOrder(PlaceOrderDto placeOrderDto) {
        try {
            PublishMessageResponse response = zeebeClient.newPublishMessageCommand()
                    .messageName("orderPlaced")
                    .correlationKey(placeOrderDto.getOrderId())
                    .variables(placeOrderDto.toVariableMap())
                    .send()
                    .join();

            log.info("Successfully published message with response {} ", response);
            return response;
        } catch (
                Exception e) {
            log.error("Failed to publish message: {}", e.getMessage());
            throw new RuntimeException("Failed to publish message", e);
        }
    }
}
