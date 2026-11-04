package com.vendor.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vendor.entity.Product;
import com.vendor.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAvailableProducts() {
        return productRepository
                .findByProductStatusIgnoreCaseAndApprovalStatusIgnoreCase(
                        "active",
                        "approved"
                );
    }
}