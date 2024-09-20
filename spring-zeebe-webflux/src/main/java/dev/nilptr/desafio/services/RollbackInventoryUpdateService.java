package dev.nilptr.desafio.services;

import dev.nilptr.desafio.dtos.RollbackInventoryUpdateDto;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class RollbackInventoryUpdateService {
    private final ZeebeClient zeebeClient;

    public Mono<String> rollbackInventoryUpdate(RollbackInventoryUpdateDto rollbackInventoryUpdateDto) {
        var message = String.format("Rolling back inventory update for %d of product %s", rollbackInventoryUpdateDto.getAmount(), rollbackInventoryUpdateDto.getProductId());
        log.info(message);
        var future = CompletableFuture.supplyAsync(() -> message);
        return Mono.fromFuture(future);
    }
}
