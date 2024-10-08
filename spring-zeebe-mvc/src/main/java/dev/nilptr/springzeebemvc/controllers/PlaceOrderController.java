package dev.nilptr.springzeebemvc.controllers;

import dev.nilptr.springzeebemvc.dtos.PlaceOrderDto;
import dev.nilptr.springzeebemvc.services.PlaceOrderService;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PlaceOrderController {
    private final PlaceOrderService placeOrderService;

    @PostMapping("/place-order")
    public CompletableFuture<PublishMessageResponse> placeOrder(@RequestBody PlaceOrderDto orderDto) {
        return placeOrderService.placeOrder(orderDto);
    }
}
