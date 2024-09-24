package dev.nilptr.springzeebemvc.services;

import dev.nilptr.springzeebemvc.consts.PaymentStatus;
import dev.nilptr.springzeebemvc.dtos.PlaceOrderDto;
import dev.nilptr.springzeebemvc.dtos.ProcessPaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProcessPaymentService {

    public CompletableFuture<ProcessPaymentDto> processPayment(PlaceOrderDto placeOrderDto) {
        return CompletableFuture.supplyAsync(() -> {
            ProcessPaymentDto dto = new ProcessPaymentDto(placeOrderDto);

            log.info("Processing payment with data " + placeOrderDto);

            if (placeOrderDto.getTotal().mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                dto.setProcessId(placeOrderDto.getOrderId());
                dto.setPaymentStatus(PaymentStatus.APPROVED);
                log.info("PAYMENT STATUS " + dto.getPaymentStatus().name());
            } else {
                dto.setProcessId(placeOrderDto.getOrderId());
                dto.setPaymentStatus(PaymentStatus.REFUSED);
                log.info("PAYMENT STATUS " + dto.getPaymentStatus().name());
            }

            return dto;
        });
    }
}
