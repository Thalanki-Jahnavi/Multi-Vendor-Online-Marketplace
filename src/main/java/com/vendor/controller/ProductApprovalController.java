
package com.vendor.controller;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vendor.entity.Product;
import com.vendor.entity.User;
import com.vendor.repository.ProductRepository;
import com.vendor.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class ProductApprovalController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Constructor injection
    public ProductApprovalController(
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // -----------------------------------------
    // CHECK ADMIN AUTHENTICATION
    // -----------------------------------------

    private User getAdmin(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            throw new AccessDeniedException("Please login first");
        }

        User admin = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Admin account not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new AccessDeniedException("Admin access required");
        }

        return admin;
    }

    // -----------------------------------------
    // PRODUCT APPROVAL PAGE
    // URL: /admin/product-approvals
    // -----------------------------------------

    @GetMapping("/product-approvals")
    public String showProductApprovals(
            Authentication authentication,
            Model model) {

        User admin = getAdmin(authentication);

        List<Product> allProducts = productRepository.findAll();

        List<Product> pendingProducts = allProducts.stream()
                .filter(product ->
                        "PENDING".equalsIgnoreCase(
                                product.getApprovalStatus()))
                .toList();

        long approvedCount = allProducts.stream()
                .filter(product ->
                        "APPROVED".equalsIgnoreCase(
                                product.getApprovalStatus()))
                .count();

        long rejectedCount = allProducts.stream()
                .filter(product ->
                        "REJECTED".equalsIgnoreCase(
                                product.getApprovalStatus()))
                .count();

        model.addAttribute("products", pendingProducts);
        model.addAttribute("totalPending", pendingProducts.size());
        model.addAttribute("totalApproved", approvedCount);
        model.addAttribute("totalRejected", rejectedCount);

        model.addAttribute("adminName", admin.getFirstName());
        model.addAttribute("adminEmail", admin.getEmail());
        model.addAttribute("activePage", "approvals");

        return "admin/product-approvals";
    }

    // -----------------------------------------
    // APPROVE PRODUCT
    // URL: POST /admin/product-approvals/approve/{productId}
    // -----------------------------------------

    @PostMapping("/product-approvals/approve/{productId}")
    public String approveProduct(
            @PathVariable("productId") Long productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        getAdmin(authentication);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found"));

        if (!"PENDING".equalsIgnoreCase(product.getApprovalStatus())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This product is not awaiting approval.");

            return "redirect:/admin/product-approvals";
        }

        product.setApprovalStatus("APPROVED");

        productRepository.save(product);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product approved successfully!");

        return "redirect:/admin/product-approvals";
    }

    // -----------------------------------------
    // REJECT PRODUCT
    // URL: POST /admin/product-approvals/reject/{productId}
    // -----------------------------------------

    @PostMapping("/product-approvals/reject/{productId}")
    public String rejectProduct(
            @PathVariable("productId") Long productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        getAdmin(authentication);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found"));

        if (!"PENDING".equalsIgnoreCase(product.getApprovalStatus())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This product is not awaiting approval.");

            return "redirect:/admin/product-approvals";
        }

        product.setApprovalStatus("REJECTED");

        productRepository.save(product);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product rejected successfully!");

        return "redirect:/admin/product-approvals";
    }

}
