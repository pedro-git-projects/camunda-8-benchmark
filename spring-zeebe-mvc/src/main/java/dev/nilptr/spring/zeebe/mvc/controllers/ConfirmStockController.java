package dev.nilptr.spring.zeebe.mvc.controllers;

import dev.nilptr.spring.zeebe.mvc.dtos.ConfirmStockDto;
import dev.nilptr.spring.zeebe.mvc.services.ConfirmStockService;
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
