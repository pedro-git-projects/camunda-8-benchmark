package dev.nilptr.desafio.dtos;

import dev.nilptr.desafio.consts.OrderStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public class CompleteOrderDto {
    private String customerEmail;
    private String orderId;
    private OrderStatus orderStatus;
    private String completionMessage;

    public CompleteOrderDto(SendOrderConfirmationDto dto) {
        this.customerEmail = dto.getCustomerEmail();
        this.orderId = dto.getOrderId();
        this.orderStatus = dto.getOrderStatus();
    }

    public Map<String, Object> toVariableMap() {
        return Map.of("customerEmail", customerEmail, "orderId", orderId, "orderStatus", orderStatus, "completionMessage", completionMessage);
    }
}
