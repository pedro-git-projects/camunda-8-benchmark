package dev.nilptr.desafio.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CancelPaymentDto {
    private String processId;
    private String orderId;

    public Map<String, Object> toVariableMap() {
        return Map.of("processId", processId, "orderId", orderId);
    }
}