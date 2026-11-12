
package com.vendor.service;

import com.vendor.entity.Category;
import com.vendor.repository.CategoryRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Get all categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Get active categories only
    public List<Category> getActiveCategoryList() {
        return categoryRepository
                .findByActiveTrueOrderByCategoryNameAsc();
    }

    // Search categories
    public List<Category> searchCategories(String keyword) {
        return categoryRepository
                .findByCategoryNameContainingIgnoreCase(keyword);
    }

    // Total categories
    public long getTotalCategories() {
        return categoryRepository.count();
    }

    // Active categories count
    public long getActiveCategories() {
        return categoryRepository.countByActiveTrue();
    }

    // Inactive categories count
    public long getInactiveCategories() {
        return categoryRepository.countByActiveFalse();
    }

    // Find category by ID
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Category not found: " + id
                        )
                );
    }

    // Save category
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Delete category
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // Activate / Deactivate category
    public void toggleCategoryStatus(Long id) {

        Category category = getCategoryById(id);

        category.setActive(
                !Boolean.TRUE.equals(category.getActive())
        );

        categoryRepository.save(category);
    }
}
