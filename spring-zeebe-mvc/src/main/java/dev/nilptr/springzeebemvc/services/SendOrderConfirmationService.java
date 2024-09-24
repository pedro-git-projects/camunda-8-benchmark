package dev.nilptr.springzeebemvc.services;

import dev.nilptr.springzeebemvc.consts.OrderStatus;
import dev.nilptr.springzeebemvc.dtos.SendOrderConfirmationDto;
import dev.nilptr.springzeebemvc.dtos.UpdateInventoryDto;
import dev.nilptr.springzeebemvc.utils.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class SendOrderConfirmationService {
    public CompletableFuture<SendOrderConfirmationDto> sendOrderConfirmation(UpdateInventoryDto updateInventoryDto) {
        return CompletableFuture.supplyAsync(() -> {
            SendOrderConfirmationDto dto = new SendOrderConfirmationDto(updateInventoryDto);

            if (Strings.isNumeric(dto.getOrderId())) {
                dto.setAccepted(true);
                dto.setOrderStatus(OrderStatus.SHIPPED);
            } else {
                dto.setAccepted(false);
                dto.setOrderStatus(OrderStatus.CANCELLED);
            }

            return dto;
        });
    }
}
