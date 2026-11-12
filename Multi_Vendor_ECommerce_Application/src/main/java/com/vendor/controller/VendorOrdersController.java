
package com.vendor.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vendor.dto.VendorOrderView;
import com.vendor.entity.Order;
import com.vendor.entity.OrderItem;
import com.vendor.entity.Product;
import com.vendor.entity.User;
import com.vendor.repository.OrderItemRepository;
import com.vendor.repository.OrderRepository;
import com.vendor.repository.ProductRepository;
import com.vendor.repository.UserRepository;

@Controller
public class VendorOrdersController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public VendorOrdersController(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Display orders containing this vendor's products.
    @GetMapping("/vendor/orders")
    public String showVendorOrders(
            Authentication authentication,
            Model model) {

        Optional<User> vendorOptional =
                getLoggedInVendor(authentication);

        if (vendorOptional.isEmpty()) {
            return "redirect:/login";
        }

        User vendor = vendorOptional.get();

        // Get products belonging to the logged-in vendor.
        List<Product> vendorProducts =
                productRepository.findByVendorIdOrderByProductIdDesc(
                        vendor.getUserId());

        // Store vendor product IDs.
        Set<Long> vendorProductIds = new HashSet<>();

        for (Product product : vendorProducts) {
            if (product.getProductId() != null) {
                vendorProductIds.add(product.getProductId());
            }
        }

        // Get only orders containing this vendor's products.
        List<Order> matchingOrders =
                orderRepository.findVendorOrders(vendor.getUserId());

        List<VendorOrderView> vendorOrders =
                new ArrayList<>();

        for (Order order : matchingOrders) {

            List<OrderItem> vendorItems =
                    new ArrayList<>();

            double vendorSubtotal = 0.0;

            if (order.getItems() != null) {

                for (OrderItem item : order.getItems()) {

                    // Include only this vendor's products.
                    if (item.getProductId() != null
                            && vendorProductIds.contains(
                                    item.getProductId())) {

                        vendorItems.add(item);

                        if (item.getSubtotal() != null) {
                            vendorSubtotal += item.getSubtotal();
                        } else if (item.getPrice() != null
                                && item.getQuantity() != null) {

                            vendorSubtotal +=
                                    item.getPrice() * item.getQuantity();
                        }

                        // Default older order items to CONFIRMED.
                        if (item.getFulfillmentStatus() == null
                                || item.getFulfillmentStatus().isBlank()) {

                            item.setFulfillmentStatus("CONFIRMED");
                        }
                    }
                }
            }

            // Ignore orders that do not contain this vendor's products.
            if (vendorItems.isEmpty()) {
                continue;
            }

            VendorOrderView orderView =
                    new VendorOrderView(
                            order.getOrderId(),
                            order.getOrderNumber(),
                            order.getCustomerName(),
                            order.getCustomerEmail(),
                            order.getCustomerPhone(),
                            order.getShippingAddress(),
                            order.getCreatedAt(),
                            order.getOrderStatus(),
                            order.getPaymentStatus(),
                            order.getPaymentMethod(),
                            vendorSubtotal,
                            vendorItems
                    );

            vendorOrders.add(orderView);
        }

        // Count orders still being processed.
        long processingOrders = vendorOrders.stream()
                .filter(order ->
                        !isCustomerOrderClosed(order.getOrderStatus()))
                .filter(order -> order.getItems().stream().anyMatch(item ->
                        !"DELIVERED".equalsIgnoreCase(
                                safeStatus(item.getFulfillmentStatus()))))
                .count();

        // Count orders whose vendor items are all delivered.
        long deliveredOrders = vendorOrders.stream()
                .filter(order ->
                        !isCustomerOrderClosed(order.getOrderStatus()))
                .filter(order -> !order.getItems().isEmpty())
                .filter(order -> order.getItems().stream().allMatch(item ->
                        "DELIVERED".equalsIgnoreCase(
                                safeStatus(item.getFulfillmentStatus()))))
                .count();

        // Count cancelled customer orders.
        long cancelledOrders = vendorOrders.stream()
                .filter(order ->
                        "CANCELLED".equalsIgnoreCase(order.getOrderStatus()))
                .count();

        // Send data to Thymeleaf.
        model.addAttribute("vendor", vendor);
        model.addAttribute("vendorName", vendor.getBusinessName());
        model.addAttribute("vendorOrders", vendorOrders);
        model.addAttribute("totalOrders", vendorOrders.size());
        model.addAttribute("processingOrders", processingOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);

        return "vendor/orders";
    }

    // Update one order item's fulfilment status.
    @Transactional
    @PostMapping("/vendor/orders/item/update-status")
    public String updateVendorItemStatus(
            Authentication authentication,
            @RequestParam Long orderItemId,
            @RequestParam String fulfillmentStatus,
            RedirectAttributes redirectAttributes) {

        Optional<User> vendorOptional =
                getLoggedInVendor(authentication);

        if (vendorOptional.isEmpty()) {
            return "redirect:/login";
        }

        User vendor = vendorOptional.get();

        // Find the requested order item.
        Optional<OrderItem> itemOptional =
                orderItemRepository.findById(orderItemId);

        if (itemOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Order item was not found."
            );

            return "redirect:/vendor/orders";
        }

        OrderItem item = itemOptional.get();

        // Verify product ownership using the database.
        if (item.getProductId() == null
                || productRepository.findByProductIdAndVendorId(
                        item.getProductId(),
                        vendor.getUserId()
                ).isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "You are not allowed to update this order item."
            );

            return "redirect:/vendor/orders";
        }

        // Check the parent customer order.
        Order parentOrder = item.getOrder();

        if (parentOrder == null
                || isCustomerOrderClosed(parentOrder.getOrderStatus())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "This customer order is cancelled or under return/exchange review."
            );

            return "redirect:/vendor/orders";
        }

        // Read current status.
        String currentStatus =
                safeStatus(item.getFulfillmentStatus());

        String requestedStatus =
                fulfillmentStatus == null
                        ? ""
                        : fulfillmentStatus.trim().toUpperCase();

        // Allow only the next valid status.
        String expectedNextStatus =
                getNextStatus(currentStatus);

        if (expectedNextStatus == null
                || !expectedNextStatus.equals(requestedStatus)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Invalid status change. Follow the fulfilment sequence: CONFIRMED → PROCESSING → SHIPPED → DELIVERED."
            );

            return "redirect:/vendor/orders";
        }

        // Update only this vendor's order item.
        item.setFulfillmentStatus(requestedStatus);

        orderItemRepository.save(item);

        redirectAttributes.addFlashAttribute(
                "success",
                "Fulfilment status updated to " + requestedStatus + "."
        );

        return "redirect:/vendor/orders";
    }

    // Determine the next allowed fulfilment status.
    private String getNextStatus(String currentStatus) {

        if ("CONFIRMED".equalsIgnoreCase(currentStatus)) {
            return "PROCESSING";
        }

        if ("PROCESSING".equalsIgnoreCase(currentStatus)) {
            return "SHIPPED";
        }

        if ("SHIPPED".equalsIgnoreCase(currentStatus)) {
            return "DELIVERED";
        }

        return null;
    }

    // Handle missing status values.
    private String safeStatus(String status) {

        return status == null || status.isBlank()
                ? "CONFIRMED"
                : status.trim();
    }

    // Check whether the customer order is closed.
    private boolean isCustomerOrderClosed(String status) {

        return "CANCELLED".equalsIgnoreCase(status)
                || "RETURN_REQUESTED".equalsIgnoreCase(status)
                || "EXCHANGE_REQUESTED".equalsIgnoreCase(status);
    }

    // Find the currently logged-in vendor.
    private Optional<User> getLoggedInVendor(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            return Optional.empty();
        }

        Optional<User> userOptional =
                userRepository.findByEmail(authentication.getName());

        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();

        if (user.getRole() == null
                || !"vendor".equalsIgnoreCase(user.getRole())) {

            return Optional.empty();
        }

        return Optional.of(user);
    }
}
