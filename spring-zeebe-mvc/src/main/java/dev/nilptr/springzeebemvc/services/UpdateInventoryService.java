package dev.nilptr.springzeebemvc.services;

import dev.nilptr.springzeebemvc.consts.OrderStatus;
import dev.nilptr.springzeebemvc.dtos.ConfirmStockDto;
import dev.nilptr.springzeebemvc.dtos.UpdateInventoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdateInventoryService {
    public CompletableFuture<UpdateInventoryDto> updateInventory(ConfirmStockDto confirmStockDto) {
        return CompletableFuture.supplyAsync(() -> {
            UpdateInventoryDto dto = new UpdateInventoryDto(confirmStockDto);

            if (dto.getAmount() % 2 == 0) {
                dto.setOrderStatus(OrderStatus.SHIPPED);
            } else {
                dto.setOrderStatus(OrderStatus.CANCELLED);
            }

            return dto;
        });
    }
}
