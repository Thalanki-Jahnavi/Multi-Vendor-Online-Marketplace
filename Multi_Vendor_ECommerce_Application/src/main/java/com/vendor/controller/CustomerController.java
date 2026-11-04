package com.vendor.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.vendor.entity.User;
import com.vendor.repository.UserRepository;
import com.vendor.service.ProductService;

@Controller
public class CustomerController {

    private final UserRepository userRepository;
    private final ProductService productService;

    public CustomerController(
            UserRepository userRepository,
            ProductService productService) {
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @GetMapping("/customer/dashboard")
    public String showCustomerDashboard(
            Authentication authentication,
            Model model) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Signed-in user was not found"));

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("products", productService.getApprovedProducts());

        return "customer/dashboard";
    }
}