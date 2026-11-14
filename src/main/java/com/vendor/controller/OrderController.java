package com.vendor.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vendor.entity.Order;
import com.vendor.entity.User;
import com.vendor.repository.UserRepository;
import com.vendor.service.OrderService;

@Controller
public class OrderController {

    private final UserRepository userRepository;
    private final OrderService orderService;

    public OrderController(UserRepository userRepository, OrderService orderService) {
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    /**
     * 1. Displays all orders placed by the currently logged-in customer
     */
    @GetMapping("/customer/orders")
    public String showCustomerOrdersPage(
            Authentication authentication,
            @RequestParam(required = false) String msg,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        List<Order> orders = orderService.getOrdersByCustomer(user.getUserId());

        model.addAttribute("customerName", user.getFirstName());
        model.addAttribute("customerFullName",
                (user.getFirstName() != null ? user.getFirstName() : "")
                + " " + (user.getLastName() != null ? user.getLastName() : ""));
        model.addAttribute("customerEmail", user.getEmail());
        model.addAttribute("orders", orders);
        model.addAttribute("msg", msg);

        return "customer/orders";
    }

    /**
     * 2. Cancel Order & Refund
     */
    @PostMapping("/customer/orders/cancel")
    public String cancelOrder(
            @RequestParam Long orderId,
            Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
                orderService.cancelOrder(orderId, user.getUserId());
            });
        }

        return "redirect:/customer/orders?msg=cancelled";
    }

    /**
     * 3. Submit Return or Exchange Request
     */
    @PostMapping("/customer/orders/return-exchange")
    public String requestReturnOrExchange(
            @RequestParam Long orderId,
            @RequestParam String requestType,
            @RequestParam String reason,
            Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
                orderService.requestReturnOrExchange(orderId, user.getUserId(), requestType, reason);
            });
        }

        return "redirect:/customer/orders?msg=request_submitted";
    }
}