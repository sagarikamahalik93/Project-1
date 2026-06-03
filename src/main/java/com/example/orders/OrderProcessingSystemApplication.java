package com.example.orders;

import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Main Application Class for Order Processing System
 */
@SpringBootApplication
@EnableAsync
public class OrderProcessingSystemApplication {

    @Value("${pulsar.broker-service-url:pulsar://localhost:6650}")
    private String pulsarBrokerUrl;

    public static void main(String[] args) {
        SpringApplication.run(OrderProcessingSystemApplication.class, args);
    }

    /**
     * Configure Pulsar Client Bean
     * 
     * @return PulsarClient instance
     * @throws Exception
     */
    @Bean
    public PulsarClient pulsarClient() throws Exception {
        return PulsarClient.builder()
                .serviceUrl(pulsarBrokerUrl)
                .build();
    }

    /**
     * Configure ObjectMapper Bean
     * Handles JSON serialization/deserialization with Java Time support
     * 
     * @return ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}
