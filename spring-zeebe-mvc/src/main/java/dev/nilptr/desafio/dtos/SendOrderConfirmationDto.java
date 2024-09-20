package dev.nilptr.desafio.dtos;

import dev.nilptr.desafio.consts.OrderStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class SendOrderConfirmationDto {
    private String customerEmail;
    private String orderId;
    private String productId;
    private boolean accepted;
    private OrderStatus orderStatus;

    public SendOrderConfirmationDto(UpdateInventoryDto dto) {
        this.orderId = dto.getOrderId();
        this.productId = dto.getProductId();
        this.customerEmail = dto.getCustomerEmail();
    }

    public Map<String, Object> toVariableMap() {
        return Map.of("customerEmail", customerEmail, "orderId", orderId, "orderStatus", orderStatus, "productId", productId, "accepted", accepted);
    }
}
