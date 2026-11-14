
package com.vendor.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vendor.entity.Product;
import com.vendor.entity.User;
import com.vendor.entity.Category;

import com.vendor.repository.ProductRepository;
import com.vendor.repository.UserRepository;

import com.vendor.service.CategoryService;
import com.vendor.service.StoreImageStorageService;

@Controller
public class AddProductController {

    private static final long MAX_PRODUCT_IMAGE_SIZE =
            5L * 1024 * 1024;

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StoreImageStorageService imageStorageService;
    private final CategoryService categoryService;

    // =========================================
    // CONSTRUCTOR
    // =========================================

    public AddProductController(
            ProductRepository productRepository,
            UserRepository userRepository,
            StoreImageStorageService imageStorageService,
            CategoryService categoryService) {

        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
        this.categoryService = categoryService;
    }

    // =========================================
    // DISPLAY ADD PRODUCT FORM
    // =========================================

    @GetMapping("/vendor/products/add")
    public String showAddProductForm(
            Authentication authentication,
            Model model) {

        User vendor = getLoggedInVendor(authentication);

        if (vendor == null) {
            return "redirect:/login";
        }

        model.addAttribute("vendor", vendor);

        // Fetch active categories from database
        List<Category> categories =
                categoryService.getActiveCategoryList();

        // CATEGORY DEBUGGING
        System.out.println();
        System.out.println("========== CATEGORY DEBUG ==========");

        System.out.println(
                "Active category count: " + categories.size()
        );

        for (Category category : categories) {

            System.out.println(
                    "Category: " + category.getCategoryName()
                    + " | Active: " + category.getActive()
            );
        }

        System.out.println("====================================");
        System.out.println();

        // Send categories to Thymeleaf
        model.addAttribute("categories", categories);

        return "vendor/add-product";
    }

    // =========================================
    // SAVE NEW PRODUCT
    // =========================================

    @PostMapping("/vendor/products/add")
    public String addProduct(
            Authentication authentication,

            @RequestParam String productName,
            @RequestParam(required = false) String description,
            @RequestParam String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String sku,
            @RequestParam Double price,
            @RequestParam(required = false) Double discountPercentage,
            @RequestParam Integer stockQuantity,
            @RequestParam(required = false) MultipartFile productImage,

            RedirectAttributes redirectAttributes,
            Model model) {

        User vendor = getLoggedInVendor(authentication);

        if (vendor == null) {
            return "redirect:/login";
        }

        // Validate product name
        if (productName == null || productName.isBlank()) {

            return showError(
                    model,
                    vendor,
                    "Product name is required."
            );
        }

        if (productName.trim().length() > 255) {

            return showError(
                    model,
                    vendor,
                    "Product name must be 255 characters or fewer."
            );
        }

        // Validate description
        if (description != null
                && description.trim().length() > 255) {

            return showError(
                    model,
                    vendor,
                    "Description must be 255 characters or fewer."
            );
        }

        // Validate category
        if (category == null || category.isBlank()) {

            return showError(
                    model,
                    vendor,
                    "Please select a category."
            );
        }

        String selectedCategory = category.trim();

        // Check whether selected category is active
        List<Category> activeCategories =
                categoryService.getActiveCategoryList();

        boolean validCategory = activeCategories.stream()
                .anyMatch(c ->
                        c.getCategoryName().equalsIgnoreCase(
                                selectedCategory
                        )
                );

        if (!validCategory) {

            return showError(
                    model,
                    vendor,
                    "Selected category is invalid or inactive."
            );
        }

        // Validate brand
        if (brand != null && brand.trim().length() > 255) {

            return showError(
                    model,
                    vendor,
                    "Brand must be 255 characters or fewer."
            );
        }

        // Validate SKU
        if (sku != null && sku.trim().length() > 255) {

            return showError(
                    model,
                    vendor,
                    "SKU must be 255 characters or fewer."
            );
        }

        // Validate price
        if (price == null || !Double.isFinite(price) || price <= 0) {

            return showError(
                    model,
                    vendor,
                    "Enter a valid product price."
            );
        }

        // Validate stock
        if (stockQuantity == null || stockQuantity < 0) {

            return showError(
                    model,
                    vendor,
                    "Stock quantity cannot be negative."
            );
        }

        // Default discount
        if (discountPercentage == null) {
            discountPercentage = 0.0;
        }

        // Validate discount
        if (!Double.isFinite(discountPercentage)
                || discountPercentage < 0
                || discountPercentage > 100) {

            return showError(
                    model,
                    vendor,
                    "Discount must be between 0 and 100."
            );
        }

        // Validate product image
        if (productImage == null || productImage.isEmpty()) {

            return showError(
                    model,
                    vendor,
                    "Please upload a product image."
            );
        }

        String imageUrl = null;

        try {

            // Save product image
            imageUrl = imageStorageService.saveImage(
                    productImage,
                    vendor.getUserId(),
                    "product",
                    MAX_PRODUCT_IMAGE_SIZE
            );

            // Create product
            Product product = new Product();

            product.setProductName(productName.trim());

            product.setDescription(
                    description == null || description.isBlank()
                            ? null
                            : description.trim()
            );

            // Save selected category
            product.setCategory(selectedCategory);

            product.setBrand(
                    brand == null || brand.isBlank()
                            ? null
                            : brand.trim()
            );

            product.setSku(
                    sku == null || sku.isBlank()
                            ? null
                            : sku.trim()
            );

            product.setPrice(price);

            product.setDiscountPercentage(discountPercentage);

            product.setStockQuantity(stockQuantity);

            // Associate vendor
            product.setVendorId(vendor.getUserId());

            // Save image URL
            product.setImageFileName(imageUrl);

            // Product status
            product.setProductStatus("ACTIVE");

            // Admin approval required
            product.setApprovalStatus("PENDING");

            // Save product
            productRepository.save(product);

        } catch (IOException | IllegalArgumentException exception) {

            if (imageUrl != null) {

                imageStorageService.deletePreviousImage(
                        imageUrl,
                        vendor.getUserId()
                );
            }

            return showError(
                    model,
                    vendor,
                    exception.getMessage() != null
                            ? exception.getMessage()
                            : "Unable to upload product image."
            );

        } catch (RuntimeException exception) {

            if (imageUrl != null) {

                imageStorageService.deletePreviousImage(
                        imageUrl,
                        vendor.getUserId()
                );
            }

            throw exception;
        }

        redirectAttributes.addFlashAttribute(
                "success",
                "Product added successfully. It is waiting for admin approval."
        );

        return "redirect:/vendor/products";
    }

    // =========================================
    // DISPLAY VALIDATION ERRORS
    // =========================================

    private String showError(
            Model model,
            User vendor,
            String message) {

        model.addAttribute("vendor", vendor);

        model.addAttribute("error", message);

        // Reload active categories
        List<Category> categories =
                categoryService.getActiveCategoryList();

        model.addAttribute("categories", categories);

        return "vendor/add-product";
    }

    // =========================================
    // GET LOGGED-IN VENDOR
    // =========================================

    private User getLoggedInVendor(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            return null;
        }

        Optional<User> userOptional =
                userRepository.findByEmail(
                        authentication.getName()
                );

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if (user.getRole() == null
                || !"vendor".equalsIgnoreCase(user.getRole())) {

            return null;
        }

        return user;
    }
}
