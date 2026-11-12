
package com.vendor.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vendor.entity.Product;
import com.vendor.entity.User;
import com.vendor.repository.ProductRepository;
import com.vendor.repository.UserRepository;

@Controller
public class InventoryController {

    private static final int LOW_STOCK_LIMIT = 5;

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public InventoryController(
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Display Inventory Page
    @GetMapping("/vendor/inventory")
    public String showInventoryPage(
            Authentication authentication,
            Model model) {

        Optional<User> vendorOptional =
                getLoggedInVendor(authentication);

        if (vendorOptional.isEmpty()) {
            return "redirect:/login";
        }

        User vendor = vendorOptional.get();

        // Get products belonging only to the logged-in vendor
        List<Product> products =
                productRepository.findByVendorIdOrderByProductIdDesc(
                        vendor.getUserId()
                );

        int totalUnits = 0;
        int lowStockCount = 0;
        int outOfStockCount = 0;

        for (Product product : products) {

            int stock = product.getStockQuantity() == null
                    ? 0 : product.getStockQuantity();

            totalUnits += stock;

            if (stock <= 0) {
                outOfStockCount++;
            } else if (stock <= LOW_STOCK_LIMIT) {
                lowStockCount++;
            }
        }

        // Send data to inventory.html
        model.addAttribute("vendor", vendor);
        model.addAttribute("vendorName", vendor.getBusinessName());

        model.addAttribute("products", products);
        model.addAttribute("totalProducts", products.size());
        model.addAttribute("totalUnits", totalUnits);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("lowStockLimit", LOW_STOCK_LIMIT);

        return "vendor/inventory";
    }

    // Update product stock
    @PostMapping("/vendor/inventory/update")
    public String updateStock(
            Authentication authentication,
            @RequestParam Long productId,
            @RequestParam Integer stockQuantity,
            RedirectAttributes redirectAttributes) {

        Optional<User> vendorOptional =
                getLoggedInVendor(authentication);

        if (vendorOptional.isEmpty()) {
            return "redirect:/login";
        }

        User vendor = vendorOptional.get();

        // Validate stock quantity
        if (stockQuantity == null || stockQuantity < 0) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Stock quantity cannot be negative."
            );

            return "redirect:/vendor/inventory";
        }

        // Verify that this product belongs to this vendor
        Optional<Product> productOptional =
                productRepository.findByProductIdAndVendorId(
                        productId,
                        vendor.getUserId()
                );

        if (productOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Product not found in your store."
            );

            return "redirect:/vendor/inventory";
        }

        Product product = productOptional.get();

        product.setStockQuantity(stockQuantity);

        productRepository.save(product);

        redirectAttributes.addFlashAttribute(
                "success",
                "Stock updated successfully for "
                        + product.getProductName() + "."
        );

        return "redirect:/vendor/inventory";
    }

    // Find currently logged-in vendor
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
