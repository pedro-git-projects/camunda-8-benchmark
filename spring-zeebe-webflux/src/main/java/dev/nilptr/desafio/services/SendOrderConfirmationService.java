package dev.nilptr.desafio.services;

import dev.nilptr.desafio.consts.OrderStatus;
import dev.nilptr.desafio.dtos.SendOrderConfirmationDto;
import dev.nilptr.desafio.dtos.UpdateInventoryDto;
import dev.nilptr.desafio.utils.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class SendOrderConfirmationService {
    public Mono<SendOrderConfirmationDto> sendOrderConfirmation(UpdateInventoryDto updateInventoryDto) {
        return Mono.create(sink -> {
            var dto = new SendOrderConfirmationDto(updateInventoryDto);
            if (Strings.isNumeric(dto.getOrderId())) {
                dto.setAccepted(true);
                dto.setOrderStatus(OrderStatus.SHIPPED);
            } else {
                dto.setAccepted(false);
                dto.setOrderStatus(OrderStatus.CANCELLED);
            }
            sink.success(dto);
        });
    }
}
