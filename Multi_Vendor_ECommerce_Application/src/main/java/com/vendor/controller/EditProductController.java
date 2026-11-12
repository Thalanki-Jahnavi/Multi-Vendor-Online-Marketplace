
package com.vendor.controller;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vendor.entity.Product;
import com.vendor.entity.User;
import com.vendor.repository.ProductRepository;
import com.vendor.repository.UserRepository;

@Controller
public class EditProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public EditProductController(
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Display edit product form
    @GetMapping("/vendor/products/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        User vendor = getLoggedInVendor(authentication);

        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isEmpty()
                || !vendor.getUserId().equals(productOptional.get().getVendorId())) {

            redirectAttributes.addFlashAttribute(
                    "error", "Product not found or access denied.");

            return "redirect:/vendor/products";
        }

        model.addAttribute("vendor", vendor);
        model.addAttribute("product", productOptional.get());

        return "vendor/edit-product";
    }

    // Update product
    @PostMapping("/vendor/products/edit/{id}")
    public String updateProduct(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam String productName,
            @RequestParam String category,
            @RequestParam Double price,
            @RequestParam(required = false) Double discountPercentage,
            @RequestParam Integer stockQuantity,
            RedirectAttributes redirectAttributes) {

        User vendor = getLoggedInVendor(authentication);

        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isEmpty()
                || !vendor.getUserId().equals(productOptional.get().getVendorId())) {

            redirectAttributes.addFlashAttribute(
                    "error", "Product not found or access denied.");

            return "redirect:/vendor/products";
        }

        if (productName == null || productName.isBlank()
                || category == null || category.isBlank()
                || price == null || price <= 0
                || stockQuantity == null || stockQuantity < 0) {

            redirectAttributes.addFlashAttribute(
                    "error", "Please enter valid product details.");

            return "redirect:/vendor/products/edit/" + id;
        }

        if (discountPercentage == null) {
            discountPercentage = 0.0;
        }

        if (discountPercentage < 0 || discountPercentage > 100) {
            redirectAttributes.addFlashAttribute(
                    "error", "Discount must be between 0 and 100.");

            return "redirect:/vendor/products/edit/" + id;
        }

        Product product = productOptional.get();

        product.setProductName(productName.trim());
        product.setCategory(category.trim());
        product.setPrice(price);
        product.setDiscountPercentage(discountPercentage);
        product.setStockQuantity(stockQuantity);

        productRepository.save(product);

        redirectAttributes.addFlashAttribute(
                "success", "Product updated successfully.");

        return "redirect:/vendor/products";
    }

    private User getLoggedInVendor(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Optional<User> userOptional =
                userRepository.findByEmail(authentication.getName());

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if (!"vendor".equalsIgnoreCase(user.getRole())) {
            return null;
        }

        return user;
    }
}
