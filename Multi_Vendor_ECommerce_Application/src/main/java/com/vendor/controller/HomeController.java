package com.vendor.controller;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.vendor.entity.Product;
import com.vendor.service.ProductService;
import com.vendor.service.VendorService;

@Controller
public class HomeController {

    private final ProductService productService;
    private final VendorService vendorService;

    public HomeController(
            ProductService productService,
            VendorService vendorService) {
        this.productService = productService;
        this.vendorService = vendorService;
    }

    @GetMapping("/")
    public String home(Model model) {

        List<Product> products = productService.getAvailableProducts();

        model.addAttribute("products", products);
        model.addAttribute("vendors", vendorService.getVendors());

        List<String> categories = products.stream()
                .map(Product::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        model.addAttribute("categories", categories);

        return "home";
    }
}