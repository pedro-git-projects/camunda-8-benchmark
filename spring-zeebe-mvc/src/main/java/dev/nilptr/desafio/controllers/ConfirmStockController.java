package dev.nilptr.desafio.controllers;

import dev.nilptr.desafio.dtos.ConfirmStockDto;
import dev.nilptr.desafio.services.ConfirmStockService;
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
public class ConfirmStockController {
    private final ConfirmStockService confirmStockService;

    @PostMapping("/confirm-stock")
    public Mono<PublishMessageResponse> confirmStock(@RequestBody ConfirmStockDto confirmStockDto) {
        return confirmStockService.confirmStock(confirmStockDto);
    }
}
