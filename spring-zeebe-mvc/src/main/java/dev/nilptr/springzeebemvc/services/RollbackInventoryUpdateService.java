package dev.nilptr.springzeebemvc.services;

import dev.nilptr.springzeebemvc.dtos.RollbackInventoryUpdateDto;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class RollbackInventoryUpdateService {

    private final ZeebeClient zeebeClient;

    public CompletableFuture<String> rollbackInventoryUpdate(RollbackInventoryUpdateDto rollbackInventoryUpdateDto) {
        String message = String.format("Rolling back inventory update for %d of product %s",
                rollbackInventoryUpdateDto.getAmount(), rollbackInventoryUpdateDto.getProductId());

        log.info(message);

        return CompletableFuture.supplyAsync(() -> message);
    }
}
