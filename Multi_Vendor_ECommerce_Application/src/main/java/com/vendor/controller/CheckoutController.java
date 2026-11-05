package com.vendor.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.vendor.entity.Order;
import com.vendor.entity.OrderItem;
import com.vendor.entity.User;
import com.vendor.repository.UserRepository;
import com.vendor.service.OrderService;

@Controller
public class CheckoutController {

    private final UserRepository userRepository;
    private final OrderService orderService;

    public CheckoutController(UserRepository userRepository, OrderService orderService) {
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    /**
     * 1. Displays Checkout Page
     */
    @GetMapping("/customer/checkout")
    public String showCheckoutPage(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
            model.addAttribute("customerName", user.getFirstName());
            model.addAttribute("customerFullName",
                    (user.getFirstName() != null ? user.getFirstName() : "")
                    + " " + (user.getLastName() != null ? user.getLastName() : ""));
            model.addAttribute("customerEmail", user.getEmail());
            model.addAttribute("userId", user.getUserId());
        });

        return "customer/checkout";
    }

    /**
     * 2. Processes Payment & Creates Order (With Robust Error Handling)
     */
    @PostMapping("/customer/checkout/pay")
    @ResponseBody
    public ResponseEntity<?> processPaymentAndCreateOrder(
            Authentication authentication,
            @RequestBody Map<String, Object> payload) {

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized. Please log in."));
            }

            Optional<User> userOptional = userRepository.findByEmail(authentication.getName());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("success", false, "error", "Customer record not found."));
            }

            User user = userOptional.get();

            String fullName = payload.get("fullName") != null ? payload.get("fullName").toString() : user.getFirstName();
            String email = user.getEmail();
            String phone = payload.get("phone") != null ? payload.get("phone").toString() : "9876543210";
            String address = payload.get("address") != null ? payload.get("address").toString() : "Address not provided";
            String paymentMethod = payload.get("paymentMethod") != null ? payload.get("paymentMethod").toString() : "CARD";

            // Safe parsing of total amount
            Double totalAmount = 0.0;
            if (payload.get("totalAmount") != null) {
                try {
                    String cleanTotal = payload.get("totalAmount").toString().replaceAll("[^0-9.]", "");
                    totalAmount = Double.valueOf(cleanTotal);
                } catch (Exception ignored) {}
            }

            // Safe parsing of order items
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cartItemsData = (List<Map<String, Object>>) payload.get("items");

            List<OrderItem> items = new ArrayList<>();
            if (cartItemsData != null) {
                for (Map<String, Object> itemMap : cartItemsData) {
                    Long productId = null;
                    if (itemMap.get("id") != null) {
                        try {
                            String cleanId = itemMap.get("id").toString().replaceAll("[^0-9]", "");
                            if (!cleanId.isEmpty()) {
                                productId = Long.valueOf(cleanId);
                            }
                        } catch (Exception ignored) {}
                    }

                    String productName = itemMap.get("name") != null ? itemMap.get("name").toString() : "Product";

                    Double price = 0.0;
                    if (itemMap.get("price") != null) {
                        try {
                            String cleanPrice = itemMap.get("price").toString().replaceAll("[^0-9.]", "");
                            price = Double.valueOf(cleanPrice);
                        } catch (Exception ignored) {}
                    }

                    Integer quantity = 1;
                    if (itemMap.get("quantity") != null) {
                        try {
                            quantity = Integer.valueOf(itemMap.get("quantity").toString());
                        } catch (Exception ignored) {}
                    }

                    String imageFileName = itemMap.get("image") != null ? itemMap.get("image").toString() : "Bluetooth Speaker.png";

                    items.add(new OrderItem(productId, productName, price, quantity, imageFileName));
                }
            }

            // Save order into MySQL database
            Order order = orderService.createOrder(
                    user.getUserId(),
                    fullName,
                    email,
                    phone,
                    address,
                    paymentMethod,
                    totalAmount,
                    items
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderNumber", order.getOrderNumber(),
                    "orderId", order.getOrderId(),
                    "message", "Payment Successful! Order Confirmed."
            ));

        } catch (Throwable t) {
            System.err.println(">>> [CHECKOUT ERROR] Failed to place order:");
            t.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Payment failed: " + (t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName())
            ));
        }
    }

    /**
     * 3. Order Success Screen
     */
    @GetMapping("/customer/checkout/success")
    public String showOrderSuccessPage(
            @RequestParam(required = false) String orderNumber,
            Authentication authentication,
            Model model) {

        if (authentication != null && authentication.isAuthenticated()) {
            userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
                model.addAttribute("customerName", user.getFirstName());
            });
        }

        if (orderNumber != null) {
            orderService.getOrderByNumber(orderNumber).ifPresent(order -> {
                model.addAttribute("order", order);
            });
        }

        return "customer/order-success";
    }
}