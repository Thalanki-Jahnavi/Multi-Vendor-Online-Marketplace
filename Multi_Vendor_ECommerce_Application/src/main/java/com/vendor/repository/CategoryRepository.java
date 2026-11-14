
package com.vendor.repository;

import com.vendor.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Search categories by name
    List<Category> findByCategoryNameContainingIgnoreCase(
            String categoryName
    );

    // Get only active categories for vendors
    List<Category> findByActiveTrueOrderByCategoryNameAsc();

    // Count active categories
    long countByActiveTrue();

    // Count inactive categories
    long countByActiveFalse();

    // Check duplicate category names
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
