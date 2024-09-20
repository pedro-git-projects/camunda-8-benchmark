package dev.nilptr.desafio.services;

import dev.nilptr.desafio.dtos.CancelPaymentDto;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class CancelPaymentService {
    private final ZeebeClient zeebeClient;

    public Mono<String> cancelPayment(CancelPaymentDto cancelPaymentDto) {
        String paymentId = cancelPaymentDto.getProcessId();
        String orderId = cancelPaymentDto.getOrderId();
        var message = String.format("Cancelled order %s and payment %s", orderId, paymentId);
        log.info(message);
        var future = CompletableFuture.supplyAsync(() -> message);
        return Mono.fromFuture(future);
    }
}
