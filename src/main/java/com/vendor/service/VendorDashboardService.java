package com.vendor.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendor.dto.VendorOrderSummary;
import com.vendor.entity.Order;
import com.vendor.entity.OrderItem;
import com.vendor.entity.Product;
import com.vendor.repository.OrderRepository;
import com.vendor.repository.ProductRepository;

@Service
public class VendorDashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public VendorDashboardService(
            ProductRepository productRepository,
            OrderRepository orderRepository) {

        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public VendorDashboardData getDashboardData(Integer vendorId) {

        List<Product> products =
                productRepository.findByVendorIdOrderByProductIdDesc(vendorId);

        List<Order> orders =
                orderRepository.findVendorOrders(vendorId);

        long totalProducts = products.size();

        long activeProducts = products.stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getProductStatus()))
                .count();

        long outOfStockProducts = products.stream()
                .filter(p -> p.getStockQuantity() == null
                        || p.getStockQuantity() <= 0)
                .count();

        long totalOrders = orders.size();

        long pendingOrders = orders.stream()
                .filter(o -> isPending(o.getOrderStatus()))
                .count();

        long completedOrders = orders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getOrderStatus()))
                .count();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1)
                .toLocalDate().atStartOfDay();

        double totalSales = 0.0;
        double monthlyRevenue = 0.0;

        List<VendorOrderSummary> recentOrders = new ArrayList<>();

        for (Order order : orders) {

            double vendorOrderTotal = 0.0;
            List<String> vendorProductNames = new ArrayList<>();

            for (OrderItem item : order.getItems()) {

                Product product = products.stream()
                        .filter(p -> p.getProductId() != null
                                && p.getProductId().equals(item.getProductId()))
                        .findFirst()
                        .orElse(null);

                if (product == null) {
                    continue;
                }

                double subtotal = getItemSubtotal(item);

                vendorOrderTotal += subtotal;

                vendorProductNames.add(
                        item.getProductName() != null
                                ? item.getProductName()
                                : product.getProductName()
                );
            }

            boolean eligibleRevenue =
                    "PAID".equalsIgnoreCase(order.getPaymentStatus())
                    && !"CANCELLED".equalsIgnoreCase(order.getOrderStatus());

            if (eligibleRevenue) {
                totalSales += vendorOrderTotal;

                if (order.getCreatedAt() != null
                        && !order.getCreatedAt().isBefore(monthStart)
                        && !order.getCreatedAt().isAfter(now)) {

                    monthlyRevenue += vendorOrderTotal;
                }
            }

            if (recentOrders.size() < 5) {

                String productSummary = vendorProductNames.isEmpty()
                        ? "Vendor products"
                        : String.join(", ", vendorProductNames);

                recentOrders.add(new VendorOrderSummary(
                        order.getOrderNumber(),
                        order.getCustomerName(),
                        order.getCreatedAt(),
                        order.getPaymentStatus(),
                        order.getOrderStatus(),
                        vendorOrderTotal,
                        productSummary
                ));
            }
        }

        List<Product> recentProducts = products.stream()
                .limit(5)
                .collect(Collectors.toList());

        return new VendorDashboardData(
                totalProducts,
                activeProducts,
                outOfStockProducts,
                totalOrders,
                pendingOrders,
                completedOrders,
                totalSales,
                monthlyRevenue,
                recentProducts,
                recentOrders
        );
    }

    private boolean isPending(String status) {

        if (status == null) {
            return false;
        }

        return status.equalsIgnoreCase("PENDING")
                || status.equalsIgnoreCase("CONFIRMED")
                || status.equalsIgnoreCase("PROCESSING");
    }

    private double getItemSubtotal(OrderItem item) {

        if (item.getSubtotal() != null) {
            return item.getSubtotal();
        }

        if (item.getPrice() != null && item.getQuantity() != null) {
            return item.getPrice() * item.getQuantity();
        }

        return 0.0;
    }

    public static class VendorDashboardData {

        private final long totalProducts;
        private final long activeProducts;
        private final long outOfStockProducts;
        private final long totalOrders;
        private final long pendingOrders;
        private final long completedOrders;
        private final double totalSales;
        private final double monthlyRevenue;
        private final List<Product> recentProducts;
        private final List<VendorOrderSummary> recentOrders;

        public VendorDashboardData(
                long totalProducts,
                long activeProducts,
                long outOfStockProducts,
                long totalOrders,
                long pendingOrders,
                long completedOrders,
                double totalSales,
                double monthlyRevenue,
                List<Product> recentProducts,
                List<VendorOrderSummary> recentOrders) {

            this.totalProducts = totalProducts;
            this.activeProducts = activeProducts;
            this.outOfStockProducts = outOfStockProducts;
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.completedOrders = completedOrders;
            this.totalSales = totalSales;
            this.monthlyRevenue = monthlyRevenue;
            this.recentProducts = recentProducts;
            this.recentOrders = recentOrders;
        }

        public long getTotalProducts() {
            return totalProducts;
        }

        public long getActiveProducts() {
            return activeProducts;
        }

        public long getOutOfStockProducts() {
            return outOfStockProducts;
        }

        public long getTotalOrders() {
            return totalOrders;
        }

        public long getPendingOrders() {
            return pendingOrders;
        }

        public long getCompletedOrders() {
            return completedOrders;
        }

        public double getTotalSales() {
            return totalSales;
        }

        public double getMonthlyRevenue() {
            return monthlyRevenue;
        }

        public List<Product> getRecentProducts() {
            return recentProducts;
        }

        public List<VendorOrderSummary> getRecentOrders() {
            return recentOrders;
        }
    }
}