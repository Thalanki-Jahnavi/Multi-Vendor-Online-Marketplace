
package com.vendor.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.vendor.entity.Product;
import com.vendor.entity.User;
import com.vendor.repository.ProductRepository;
import com.vendor.repository.UserRepository;

@Controller
public class VendorProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Constructor injection
    public VendorProductController(
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Display products belonging to the logged-in vendor
    @GetMapping("/vendor/products")
    public String showProducts(
            Authentication authentication,
            Model model) {

        // Check login
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            return "redirect:/login";
        }

        // Find logged-in user using email
        Optional<User> userOptional =
                userRepository.findByEmail(authentication.getName());

        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }

        User vendor = userOptional.get();

        // Allow only vendors
        if (!"vendor".equalsIgnoreCase(vendor.getRole())) {
            return "redirect:/login?error";
        }

        // Get vendor ID
        Integer vendorId = vendor.getUserId();

        // Fetch only this vendor's products
        List<Product> products =
                productRepository.findByVendorIdOrderByProductIdDesc(vendorId);

        // Send data to Thymeleaf
        model.addAttribute("vendor", vendor);
        model.addAttribute("products", products);
        model.addAttribute("totalProducts", products.size());

        // Open products page
        return "vendor/products";
    }
}
