package com.example.orders.service;

import com.example.orders.dto.OrderDTO;
import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.entity.Order;
import com.example.orders.repository.OrderRepository;
import com.example.orders.producer.OrderProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Order Processing
 * Handles business logic for order creation and retrieval
 */
@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProducer orderProducer;

    /**
     * Create a new order
     * Saves order to database and publishes event to Pulsar
     * 
     * @param request CreateOrderRequest
     * @return OrderDTO
     */
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        logger.info("Creating new order for customer: {}", request.getCustomerId());

        // Generate unique order ID
        String orderId = "ORD-" + UUID.randomUUID().toString();

        // Create order entity
        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerId(request.getCustomerId());
        order.setCustomerName(request.getCustomerName());
        order.setItems(request.getItems());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Save to database
        Order savedOrder = orderRepository.save(order);
        logger.info("Order saved to database: {}", orderId);

        // Publish event to Pulsar
        orderProducer.publishOrderEvent(savedOrder);
        logger.info("Order event published to Pulsar: {}", orderId);

        return mapToDTO(savedOrder);
    }

    /**
     * Get order by ID
     * 
     * @param orderId Order ID
     * @return OrderDTO
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(String orderId) {
        logger.info("Fetching order: {}", orderId);
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return mapToDTO(order);
    }

    /**
     * Get all orders for a customer
     * 
     * @param customerId Customer ID
     * @return List of OrderDTOs
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomerId(String customerId) {
        logger.info("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all orders
     * 
     * @return List of all OrderDTOs
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        logger.info("Fetching all orders");
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update order status
     * Called by consumer after processing
     * 
     * @param orderId Order ID
     * @param status New status
     */
    @Transactional
    public void updateOrderStatus(String orderId, String status) {
        logger.info("Updating order status - OrderId: {}, Status: {}", orderId, status);
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        logger.info("Order status updated: {}", orderId);
    }

    /**
     * Convert Order entity to OrderDTO
     * 
     * @param order Order entity
     * @return OrderDTO
     */
    private OrderDTO mapToDTO(Order order) {
        return new OrderDTO(
                order.getOrderId(),
                order.getCustomerId(),
                order.getCustomerName(),
                order.getItems(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
