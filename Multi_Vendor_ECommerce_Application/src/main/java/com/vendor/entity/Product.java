package com.vendor.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "vendor_id", nullable = false)
    private Integer vendorId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "brand")
    private String brand;

    @Column(name = "sku")
    private String sku;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "weight_kg", precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "color")
    private String color;

    @Column(name = "size")
    private String size;

    @Column(name = "product_status")
    private String productStatus;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "is_returnable")
    private Boolean returnable;

    @Column(name = "is_replaceable")
    private Boolean replaceable;

    @Column(name = "warranty_months")
    private Integer warrantyMonths;

    public Product() {
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public String getColor() {
        return color;
    }

    public String getSize() {
        return size;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public Boolean getReturnable() {
        return returnable;
    }

    public Boolean getReplaceable() {
        return replaceable;
    }

    public Integer getWarrantyMonths() {
        return warrantyMonths;
    }
}