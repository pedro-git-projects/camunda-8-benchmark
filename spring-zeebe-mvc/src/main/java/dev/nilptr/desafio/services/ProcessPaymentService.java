package dev.nilptr.desafio.services;

import dev.nilptr.desafio.consts.PaymentStatus;
import dev.nilptr.desafio.dtos.PlaceOrderDto;
import dev.nilptr.desafio.dtos.ProcessPaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProcessPaymentService {
    public Mono<ProcessPaymentDto> processPayment(PlaceOrderDto placeorderDto) {
        return Mono.create(sink -> {
            var dto = new ProcessPaymentDto(placeorderDto);
            log.info("Processing payment with data " + placeorderDto);
            if (placeorderDto.getTotal().mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                dto.setProcessId(placeorderDto.getOrderId());
                dto.setPaymentStatus(PaymentStatus.APPROVED);
                log.info("PAYMENT STATUS " + dto.getPaymentStatus().name());
            } else {
                dto.setProcessId(placeorderDto.getOrderId());
                dto.setPaymentStatus(PaymentStatus.REFUSED);
               log.info("PAYMENT STATUS " + dto.getPaymentStatus().name());
            }
            sink.success(dto);
        });
    }
}
