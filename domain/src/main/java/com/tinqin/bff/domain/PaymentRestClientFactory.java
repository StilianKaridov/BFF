package com.tinqin.bff.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinqin.payment.restexport.PaymentRestClient;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentRestClientFactory {

    @Bean
    public PaymentRestClient getPaymentRestClient(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return Feign.builder()
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .target(PaymentRestClient.class, "http://192.168.240.1:8083");
    }
}
