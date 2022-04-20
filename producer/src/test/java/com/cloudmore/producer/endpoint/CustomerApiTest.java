package com.cloudmore.producer.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.cloudmore.interprocess.utils.MoneyConverter;
import com.cloudmore.producer.endpoint.model.Request;
import com.cloudmore.producer.interprocess.MessageSender;
import com.cloudmore.producer.service.PriceCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest
class CustomerApiTest {

    private static final double CALCULATED_AMOUNT = 110.;

    @MockBean
    private PriceCalculator priceCalculator;
    @MockBean
    private MessageSender sender;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    private CustomerApi api;

    @BeforeEach
    void setup() {
        doReturn(CALCULATED_AMOUNT).when(priceCalculator).calculateWithTax(anyDouble());
    }

    @Test
    void test_produce() throws Exception {
        var request = createRequest();
        var builder = MockMvcRequestBuilders.post("/api/customer/receipt")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsBytes(request));
        mockMvc.perform(builder)
                .andExpect(status().isNoContent());
    }

    @Test
    void test_produce_not_fully_filled() throws Exception {
        var request = new Request();
        var builder = MockMvcRequestBuilders.post("/api/customer/receipt")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsBytes(request));
        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());

        request.setName("Test");
        builder.content(objectMapper.writeValueAsBytes(request));
        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());

        request.setSurname("Surname");
        builder.content(objectMapper.writeValueAsBytes(request));
        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());

        request.setEventTime(ZonedDateTime.now());
        builder.content(objectMapper.writeValueAsBytes(request));
        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());

        request.setWage(-1.);
        builder.content(objectMapper.writeValueAsBytes(request));
        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());

        request.setWage(0);
        builder.content(objectMapper.writeValueAsBytes(request));
        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());

        request.setWage(100.);
        builder.content(objectMapper.writeValueAsBytes(request));
        mockMvc.perform(builder)
                .andExpect(status().isNoContent());
    }

    @Test
    void test_convertToMessage() {
        var request = createRequest();
        var message = api.convertToMessage(request);
        assertFalse(message.getOperationId().isEmpty());
        assertEquals(request.getName(), message.getName());
        assertEquals(request.getSurname(), message.getSurname());
        assertEquals(request.getEventTime(), message.getEventTime());
        assertEquals(MoneyConverter.convertToCoins(CALCULATED_AMOUNT), message.getAmount());
    }

    private Request createRequest() {
        var request = new Request();
        request.setName("Test");
        request.setSurname("Testov");
        request.setWage(100.);
        request.setEventTime(ZonedDateTime.now());
        return request;
    }
}