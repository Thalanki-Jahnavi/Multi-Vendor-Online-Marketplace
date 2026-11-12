
package com.vendor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vendor.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Customer marketplace:
    // Only active, approved and in-stock products
    List<Product>
    findByProductStatusIgnoreCaseAndApprovalStatusIgnoreCaseAndStockQuantityGreaterThanOrderByProductIdDesc(
            String productStatus,
            String approvalStatus,
            Integer stockQuantity
    );

    // Get all products belonging to a vendor
    List<Product> findByVendorIdOrderByProductIdDesc(
            Integer vendorId
    );

    // Find a product only if it belongs to the vendor
    Optional<Product> findByProductIdAndVendorId(
            Long productId,
            Integer vendorId
    );
}
