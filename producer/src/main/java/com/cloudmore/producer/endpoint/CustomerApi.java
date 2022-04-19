package com.cloudmore.producer.endpoint;

import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cloudmore.interprocess.event.CustomerKafkaMessageEvent;
import com.cloudmore.interprocess.utils.MoneyConverter;
import com.cloudmore.producer.endpoint.model.Request;
import com.cloudmore.producer.interprocess.MessageSender;
import com.cloudmore.producer.service.PriceCalculator;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerApi {

    private final PriceCalculator priceCalculator;
    private final MessageSender sender;

    @Operation(summary = "Send request to consumer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Request is sent successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Cannot convert request to json", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal service error. Please contact support.",
                    content = @Content) })
    @PostMapping("/receipt")
    public ResponseEntity<Void> produce(@Valid @RequestBody Request request) {
        var message = CustomerKafkaMessageEvent.builder()
                .operationId(UUID.randomUUID().toString())
                .name(request.getName())
                .surname(request.getSurname())
                .amount(MoneyConverter.convertToCoins(priceCalculator.calculateWithTax(request.getWage())))
                .eventTime(request.getEventTime())
                .build();
        try {
            sender.send(message);
            return ResponseEntity.noContent().build();
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
