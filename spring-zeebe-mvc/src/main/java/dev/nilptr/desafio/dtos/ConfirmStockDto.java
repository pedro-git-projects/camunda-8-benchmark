package dev.nilptr.desafio.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.nilptr.desafio.consts.PaymentStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public class ConfirmStockDto {
    private String processId;
    private PaymentStatus paymentStatus;
    private String productId;
    private int amount;
    private String customerEmail;
    private String orderId;
    private boolean inStock;

       public Map<String, Object> toVariableMap() {
        return Map.of("processId", processId, "paymentStatus", paymentStatus.name(), "productId", productId, "amount", amount, "customerEmail", customerEmail, "orderId", orderId, "inStock", inStock);
    }

}
