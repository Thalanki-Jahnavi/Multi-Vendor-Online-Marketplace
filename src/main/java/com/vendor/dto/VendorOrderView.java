
package com.vendor.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.vendor.entity.OrderItem;

public class VendorOrderView {

    private Long orderId;
    private String orderNumber;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shippingAddress;

    private LocalDateTime createdAt;

    private String orderStatus;
    private String paymentStatus;
    private String paymentMethod;

    private Double vendorSubtotal;

    private List<OrderItem> items;

    public VendorOrderView(
            Long orderId,
            String orderNumber,
            String customerName,
            String customerEmail,
            String customerPhone,
            String shippingAddress,
            LocalDateTime createdAt,
            String orderStatus,
            String paymentStatus,
            String paymentMethod,
            Double vendorSubtotal,
            List<OrderItem> items) {

        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.shippingAddress = shippingAddress;
        this.createdAt = createdAt;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.vendorSubtotal = vendorSubtotal;
        this.items = items;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public Double getVendorSubtotal() {
        return vendorSubtotal;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
