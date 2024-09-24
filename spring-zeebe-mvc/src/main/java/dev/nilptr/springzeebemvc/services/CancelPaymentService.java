package dev.nilptr.springzeebemvc.services;

import dev.nilptr.springzeebemvc.dtos.CancelPaymentDto;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@Service
public class CancelPaymentService {
    private final ZeebeClient zeebeClient;

    public CompletableFuture<String> cancelPaymentAsync(CancelPaymentDto cancelPaymentDto) {
        String paymentId = cancelPaymentDto.getProcessId();
        String orderId = cancelPaymentDto.getOrderId();
        var message = String.format("Cancelled order %s and payment %s", orderId, paymentId);
        log.info(message);


        return CompletableFuture.supplyAsync(() -> {
            return message;
        });
    }

    public String cancelPayment(CancelPaymentDto cancelPaymentDto) {
        try {
            return cancelPaymentAsync(cancelPaymentDto).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while cancelling payment", e);
            throw new RuntimeException("Error cancelling payment", e);
        }
    }
}