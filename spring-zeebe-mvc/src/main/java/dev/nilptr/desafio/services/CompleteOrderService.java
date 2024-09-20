package dev.nilptr.desafio.services;

import dev.nilptr.desafio.dtos.CompleteOrderDto;
import dev.nilptr.desafio.dtos.SendOrderConfirmationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompleteOrderService {
    public Mono<CompleteOrderDto> completeOrder(SendOrderConfirmationDto sendOrderConfirmationDto) {
        return Mono.create(sink -> {
            var dto = new CompleteOrderDto(sendOrderConfirmationDto);
            var message = String.format("Completed order with id " + dto.getOrderId() + " and status " + dto.getCompletionMessage());
            dto.setCompletionMessage(message);
            sink.success(dto);
        });
    }
}
