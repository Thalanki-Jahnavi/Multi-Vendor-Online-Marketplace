package com.vendor.dto;

import java.time.LocalDateTime;

public class VendorOrderSummary {

    private String orderNumber;
    private String customerName;
    private LocalDateTime createdAt;
    private String paymentStatus;
    private String orderStatus;
    private Double vendorTotal;
    private String productSummary;

    public VendorOrderSummary() {
    }

    public VendorOrderSummary(
            String orderNumber,
            String customerName,
            LocalDateTime createdAt,
            String paymentStatus,
            String orderStatus,
            Double vendorTotal,
            String productSummary) {

        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.createdAt = createdAt;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.vendorTotal = vendorTotal;
        this.productSummary = productSummary;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public Double getVendorTotal() {
        return vendorTotal;
    }

    public String getProductSummary() {
        return productSummary;
    }
}