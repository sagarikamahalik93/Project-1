package com.example.orders.producer;

import com.example.orders.entity.Order;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Pulsar Producer for publishing order events
 * Sends order messages to Pulsar topics for async processing
 */
@Component
public class OrderProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderProducer.class);

    @Autowired
    private PulsarClient pulsarClient;

    @Value("${pulsar.topic.orders:persistent://public/default/orders-topic}")
    private String orderTopic;

    @Autowired
    private ObjectMapper objectMapper;

    private Producer<String> producer;

    /**
     * Initialize producer
     * 
     * @throws PulsarClientException
     */
    public void init() throws PulsarClientException {
        if (producer == null) {
            producer = pulsarClient.newProducer(org.apache.pulsar.client.api.Schema.STRING)
                    .topic(orderTopic)
                    .producerName("order-producer")
                    .enableBatching(true)
                    .batchingMaxPublishDelayMicros(100000)
                    .create();
            logger.info("Pulsar producer initialized for topic: {}", orderTopic);
        }
    }

    /**
     * Publish order event to Pulsar
     * 
     * @param order Order entity
     */
    public void publishOrderEvent(Order order) {
        try {
            init();
            String orderJson = objectMapper.writeValueAsString(order);
            producer.sendAsync(orderJson)
                    .thenAccept(msgId -> logger.info("Order event published - OrderId: {}, MessageId: {}", 
                            order.getOrderId(), msgId))
                    .exceptionally(ex -> {
                        logger.error("Failed to publish order event - OrderId: {}", order.getOrderId(), ex);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Error publishing order event for OrderId: {}", order.getOrderId(), e);
            throw new RuntimeException("Failed to publish order event", e);
        }
    }

    /**
     * Close producer connection
     * 
     * @throws PulsarClientException
     */
    public void close() throws PulsarClientException {
        if (producer != null) {
            producer.close();
            logger.info("Pulsar producer closed");
        }
    }
}
