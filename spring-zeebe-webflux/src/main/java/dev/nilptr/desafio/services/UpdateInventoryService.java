package dev.nilptr.desafio.services;

import dev.nilptr.desafio.consts.OrderStatus;
import dev.nilptr.desafio.dtos.ConfirmStockDto;
import dev.nilptr.desafio.dtos.UpdateInventoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdateInventoryService {
       public Mono<UpdateInventoryDto> updateInventory(ConfirmStockDto confirmStockDto) {
           return Mono.create(sink -> {
               var dto = new UpdateInventoryDto(confirmStockDto);
               if (dto.getAmount() % 2 == 0) {
                   dto.setOrderStatus(OrderStatus.SHIPPED);
               } else {
                   dto.setOrderStatus(OrderStatus.CANCELLED);
               }
               sink.success(dto);
           });
       }
}
