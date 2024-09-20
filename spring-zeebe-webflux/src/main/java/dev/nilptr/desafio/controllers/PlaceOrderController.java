package dev.nilptr.desafio.controllers;

import dev.nilptr.desafio.dtos.PlaceOrderDto;
import dev.nilptr.desafio.services.PlaceOrderService;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@Slf4j
@RequiredArgsConstructor
@RestController
public class PlaceOrderController {
    private final PlaceOrderService placeOrderService;

    @PostMapping("/place-order")
    public Mono<PublishMessageResponse> placeOrder(@RequestBody PlaceOrderDto orderDto) {
        return placeOrderService.placeOrder(orderDto);
    }
}
