package dev.nilptr.spring.zeebe.webflux.dtos;

import dev.nilptr.spring.zeebe.webflux.consts.PaymentMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;
import java.util.Map;

@Getter
@Setter
@ToString
public class PlaceOrderDto {
    private String customerEmail;
    private String orderId;
    private PaymentMethod paymentMethod;
    private BigInteger total;
    private String productId;
    private int amount;

    public Map<String, Object> toVariableMap() {
        return Map.of("orderId", orderId, "paymentMethod", paymentMethod, "total", total, "customerEmail", customerEmail, "productId", productId, "amount", amount);
    }
}
