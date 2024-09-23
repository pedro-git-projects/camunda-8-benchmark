package dev.nilptr.spring.zeebe.mvc.services;

import dev.nilptr.springzeebemvc.consts.OrderStatus;
import dev.nilptr.springzeebemvc.dtos.SendOrderConfirmationDto;
import dev.nilptr.springzeebemvc.dtos.UpdateInventoryDto;
import dev.nilptr.springzeebemvc.utils.Strings;
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
