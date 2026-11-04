package com.vendor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vendor.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByProductStatusIgnoreCaseAndApprovalStatusIgnoreCaseAndVendorId(
            String productStatus,
            String approvalStatus,
            Integer vendorId
    );

    List<Product> findByProductStatusIgnoreCaseAndApprovalStatusIgnoreCase(
            String productStatus,
            String approvalStatus
    );
}