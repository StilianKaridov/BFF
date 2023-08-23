package com.tinqin.bff.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinqin.zoostore.restexport.ZooStoreRestClient;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZooStoreRestClientFactory {

    @Bean
    public ZooStoreRestClient getZooStoreRestClient(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return Feign.builder()
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .target(ZooStoreRestClient.class, "http://192.168.240.1:8080");
    }
}
