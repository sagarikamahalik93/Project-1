package com.example.orders.dto;

/**
 * Create Order Request DTO
 * Used for API requests to create new orders
 */
public class CreateOrderRequest {

    private String customerId;
    private String customerName;
    private String items;
    private Double totalAmount;

    // Constructors
    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String customerId, String customerName, String items, Double totalAmount) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
