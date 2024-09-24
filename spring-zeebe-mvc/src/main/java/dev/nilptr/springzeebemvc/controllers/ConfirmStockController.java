package dev.nilptr.springzeebemvc.controllers;

import dev.nilptr.springzeebemvc.dtos.ConfirmStockDto;
import dev.nilptr.springzeebemvc.services.ConfirmStockService;
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
public class ConfirmStockController {
    private final ConfirmStockService confirmStockService;

    @PostMapping("/confirm-stock")
    public CompletableFuture<PublishMessageResponse> confirmStock(@RequestBody ConfirmStockDto confirmStockDto) {
        return confirmStockService.confirmStock(confirmStockDto);
    }
}
