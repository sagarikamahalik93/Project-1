package com.example.orders.consumer;

import com.example.orders.entity.Order;
import com.example.orders.service.OrderService;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Pulsar Consumer for processing order events
 * Listens to order topic and processes events asynchronously
 */
@Component
public class OrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    private PulsarClient pulsarClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${pulsar.topic.orders:persistent://public/default/orders-topic}")
    private String orderTopic;

    @Value("${pulsar.consumer.subscription:order-subscription}")
    private String subscriptionName;

    private Consumer<String> consumer;

    /**
     * Initialize consumer and start listening to Pulsar topic
     * Called after application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            consumer = pulsarClient.newConsumer(org.apache.pulsar.client.api.Schema.STRING)
                    .topic(orderTopic)
                    .subscriptionName(subscriptionName)
                    .subscriptionType(SubscriptionType.Exclusive)
                    .consumerName("order-consumer")
                    .subscribe();

            logger.info("Pulsar consumer initialized for topic: {} with subscription: {}", 
                    orderTopic, subscriptionName);

            // Start consuming messages in a separate thread
            startConsuming();
        } catch (Exception e) {
            logger.error("Error initializing Pulsar consumer", e);
            throw new RuntimeException("Failed to initialize Pulsar consumer", e);
        }
    }

    /**
     * Start consuming messages from Pulsar topic
     */
    private void startConsuming() {
        new Thread(() -> {
            while (true) {
                try {
                    Message<String> message = consumer.receive();
                    processOrderEvent(message);
                    consumer.acknowledge(message);
                } catch (Exception e) {
                    logger.error("Error consuming message from Pulsar", e);
                    try {
                        Thread.sleep(1000); // Backoff on error
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "order-consumer-thread").start();
    }

    /**
     * Process order event
     * Updates order status to PROCESSED after handling
     * 
     * @param message Pulsar message
     */
    private void processOrderEvent(Message<String> message) {
        try {
            String orderJson = message.getValue();
            Order order = objectMapper.readValue(orderJson, Order.class);
            
            logger.info("Processing order event - OrderId: {}", order.getOrderId());
            
            // Simulate order processing (validation, payment, etc.)
            simulateOrderProcessing(order);
            
            // Update order status to PROCESSED
            orderService.updateOrderStatus(order.getOrderId(), "PROCESSED");
            
            logger.info("Order processed successfully - OrderId: {}", order.getOrderId());
        } catch (Exception e) {
            logger.error("Error processing order event", e);
        }
    }

    /**
     * Simulate order processing logic
     * In a real scenario, this would include payment processing, inventory updates, etc.
     * 
     * @param order Order entity
     */
    private void simulateOrderProcessing(Order order) {
        try {
            // Simulate processing delay
            Thread.sleep(1000);
            logger.info("Order processing simulation complete for OrderId: {}", order.getOrderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Order processing interrupted for OrderId: {}", order.getOrderId(), e);
        }
    }

    /**
     * Close consumer connection
     */
    public void close() {
        try {
            if (consumer != null) {
                consumer.close();
                logger.info("Pulsar consumer closed");
            }
        } catch (Exception e) {
            logger.error("Error closing Pulsar consumer", e);
        }
    }
}
