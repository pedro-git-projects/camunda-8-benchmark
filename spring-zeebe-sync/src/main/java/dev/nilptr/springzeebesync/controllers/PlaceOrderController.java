package dev.nilptr.springzeebesync.controllers;

import dev.nilptr.springzeebesync.dtos.PlaceOrderDto;
import dev.nilptr.springzeebesync.services.PlaceOrderService;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PlaceOrderController {
    private final PlaceOrderService placeOrderService;

   @PostMapping("/place-order")
    public PublishMessageResponse placeOrder(@RequestBody PlaceOrderDto orderDto) {
       PublishMessageResponse response = placeOrderService.placeOrder(orderDto);
       log.info("Order placed sucessfully with response {} ", response);
       return response;
   }
}
