package dev.nilptr.desafio.dtos;

import dev.nilptr.desafio.consts.PaymentStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Getter
@Setter
@ToString
@Slf4j
@RequiredArgsConstructor
public class ProcessPaymentDto {
    private String processId;
    private PaymentStatus paymentStatus;
    private String productId;
    private int amount;
    private String customerEmail;
    private String orderId;

    public ProcessPaymentDto(PlaceOrderDto dto) {
        this.productId = dto.getProductId();
        this.amount = dto.getAmount();
        this.customerEmail = dto.getCustomerEmail();
        this.orderId = dto.getOrderId();
    }

    public Map<String, Object> toVariableMap() {
        return Map.of("processId", processId, "paymentStatus", paymentStatus.name(), "productId", productId, "amount", amount, "customerEmail", customerEmail, "orderId", orderId);

    }
}
