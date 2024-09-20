package dev.nilptr.desafio.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class RollbackInventoryUpdateDto {
    private String productId;
    private int amount;
}
