package dev.nilptr.springzeebemvc.services;

import dev.nilptr.springzeebemvc.dtos.CompleteOrderDto;
import dev.nilptr.springzeebemvc.dtos.SendOrderConfirmationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompleteOrderService {
    public CompletableFuture<CompleteOrderDto> completeOrder( SendOrderConfirmationDto sendOrderConfirmationDto) {
        return CompletableFuture.supplyAsync(() -> {
            CompleteOrderDto dto = new CompleteOrderDto(sendOrderConfirmationDto);
            String message = String.format("Completed order with id %s and status %s",
                    dto.getOrderId(),
                    dto.getCompletionMessage());
            dto.setCompletionMessage(message);
            return dto;
        });
    }
}
