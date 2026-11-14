
package com.vendor.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.vendor.entity.Order;
import com.vendor.entity.Product;
import com.vendor.entity.User;
import com.vendor.repository.OrderRepository;
import com.vendor.repository.ProductRepository;
import com.vendor.repository.UserRepository;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public AdminService(
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository) {

        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public Map<String, Object> getDashboardData() {

        List<User> allUsers = userRepository.findAll();
        List<Product> allProducts = productRepository.findAll();
        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc();

        List<User> customers = allUsers.stream()
                .filter(user -> isStatus(user.getRole(), "CUSTOMER"))
                .sorted(Comparator.comparing(
                        User::getUserId,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<User> vendors = allUsers.stream()
                .filter(user -> isStatus(user.getRole(), "VENDOR"))
                .sorted(Comparator.comparing(
                        User::getUserId,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<Product> pendingProducts = allProducts.stream()
                .filter(product ->
                        isStatus(product.getApprovalStatus(), "PENDING"))
                .sorted(Comparator.comparing(
                        Product::getProductId,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<Product> approvedProducts = allProducts.stream()
                .filter(product ->
                        isStatus(product.getApprovalStatus(), "APPROVED"))
                .toList();

        long pendingOrders = allOrders.stream()
                .filter(order -> isStatus(order.getOrderStatus(), "PENDING"))
                .count();

        long completedOrders = allOrders.stream()
                .filter(order -> isStatus(order.getOrderStatus(), "DELIVERED"))
                .count();

        double totalRevenue = allOrders.stream()
                .filter(order -> isStatus(order.getPaymentStatus(), "PAID"))
                .filter(order -> !isStatus(order.getOrderStatus(), "CANCELLED"))
                .mapToDouble(order ->
                        order.getTotalAmount() == null
                                ? 0.0
                                : order.getTotalAmount())
                .sum();

        List<Product> recentProducts = allProducts.stream()
                .sorted(Comparator.comparing(
                        Product::getProductId,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();

        Map<String, Object> data = new HashMap<>();

        data.put("totalCustomers", customers.size());
        data.put("totalVendors", vendors.size());
        data.put("totalProducts", allProducts.size());
        data.put("pendingApprovals", pendingProducts.size());
        data.put("approvedProducts", approvedProducts.size());
        data.put("totalOrders", allOrders.size());
        data.put("pendingOrders", pendingOrders);
        data.put("completedOrders", completedOrders);
        data.put("totalRevenue", totalRevenue);

        data.put("recentCustomers", customers.stream().limit(5).toList());
        data.put("recentVendors", vendors.stream().limit(5).toList());
        data.put("recentProducts", recentProducts);
        data.put("recentOrders", allOrders.stream().limit(5).toList());
        data.put("pendingProducts", pendingProducts.stream().limit(5).toList());

        return data;
    }

    private boolean isStatus(String actual, String expected) {
        return actual != null && actual.trim().equalsIgnoreCase(expected);
    }
}
