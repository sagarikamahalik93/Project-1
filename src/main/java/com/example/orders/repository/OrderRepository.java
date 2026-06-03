package com.example.orders.repository;

import com.example.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Order Repository
 * Data access layer for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by orderId
     * 
     * @param orderId Order ID
     * @return Optional containing Order if found
     */
    Optional<Order> findByOrderId(String orderId);

    /**
     * Find all orders for a customer
     * 
     * @param customerId Customer ID
     * @return List of orders for the customer
     */
    List<Order> findByCustomerId(String customerId);

    /**
     * Find all orders with a specific status
     * 
     * @param status Order status
     * @return List of orders with the status
     */
    List<Order> findByStatus(String status);
}
