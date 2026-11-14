
package com.vendor.controller;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vendor.entity.Product;
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

    // Redirect Customer Dashboard to Shop Products
    @GetMapping("/customer/dashboard")
    public String showCustomerDashboard() {

        return "redirect:/customer/products";
    }

    // Display Customer Shop Products Page
    @GetMapping("/customer/products")
    public String showCustomerProductsPage(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer vendorId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            Model model) {

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            userRepository.findByEmail(authentication.getName())
                    .ifPresent(user -> {

                        model.addAttribute(
                                "customerName",
                                user.getFirstName());

                        model.addAttribute(
                                "customerFullName",
                                (user.getFirstName() != null
                                        ? user.getFirstName() : "")
                                + " "
                                + (user.getLastName() != null
                                        ? user.getLastName() : ""));

                        model.addAttribute(
                                "customerEmail",
                                user.getEmail());
                    });
        }

        // Get all approved products before applying filters
        List<Product> allProducts =
                productService.getApprovedProducts();

        // Apply selected filters to product listing
        List<Product> filteredProducts =
                productService.filterProducts(
                        allProducts,
                        keyword,
                        category,
                        vendorId,
                        minPrice,
                        maxPrice,
                        sort);

        // Collect unique categories without case duplicates
        TreeSet<String> categories =
                new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        TreeSet<Integer> vendorIds = new TreeSet<>();

        // CASE-INSENSITIVE CATEGORY COUNTS
        Map<String, Long> categoryCounts =
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Product product : allProducts) {

            if (product.getCategory() != null
                    && !product.getCategory().isBlank()) {

                String categoryName =
                        product.getCategory().trim();

                categories.add(categoryName);

                categoryCounts.merge(
                        categoryName,
                        1L,
                        Long::sum);
            }

            if (product.getVendorId() != null) {
                vendorIds.add(product.getVendorId());
            }
        }

        // Get vendor details
        List<Integer> vendorIdList =
                vendorIds.stream().collect(Collectors.toList());

        List<User> vendors = vendorIdList.isEmpty()
                ? List.of()
                : userRepository.findByUserIdIn(vendorIdList);

        Map<Integer, String> vendorNames =
                vendors.stream().collect(Collectors.toMap(
                        User::getUserId,
                        user -> {

                            String businessName =
                                    user.getBusinessName();

                            if (businessName != null
                                    && !businessName.isBlank()) {
                                return businessName;
                            }

                            String firstName =
                                    user.getFirstName();

                            return firstName != null
                                    && !firstName.isBlank()
                                    ? firstName
                                    : "Vendor";
                        },
                        (existing, replacement) -> existing
                ));

        boolean hasActiveFilters =
                (keyword != null && !keyword.isBlank())
                || (category != null && !category.isBlank())
                || vendorId != null
                || minPrice != null
                || maxPrice != null;

        // Send data to Thymeleaf
        model.addAttribute("products", filteredProducts);

        // Total approved products, independent of selected filters
        model.addAttribute(
                "totalProductsCount",
                allProducts.size());

        model.addAttribute("categories", categories);

        System.out.println("========== CATEGORY COUNT DEBUG ==========");
        System.out.println("Total approved products: " + allProducts.size());
        System.out.println("Categories: " + categories);
        System.out.println("Category counts: " + categoryCounts);
        System.out.println("==========================================");

        model.addAttribute("categoryCounts", categoryCounts);

        model.addAttribute("vendors", vendors);
        model.addAttribute("vendorNames", vendorNames);

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedVendorId", vendorId);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("hasActiveFilters", hasActiveFilters);

        return "public/products";
    }
}
