
package com.vendor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "vendor_id")
    private Integer vendorId;

    @Column(name = "product_name")
    private String productName;

    private String description;
    private String category;
    private String brand;
    private String sku;
    private Double price;

    @Column(name = "discount_percentage")
    private Double discountPercentage;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "weight_kg")
    private java.math.BigDecimal weightKg;

    private String color;
    private String size;

    @Column(name = "product_status")
    private String productStatus;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "is_returnable")
    private Boolean returnable = true;

    @Column(name = "is_replaceable")
    private Boolean replaceable = false;

    @Column(name = "warranty_months")
    private Integer warrantyMonths = 0;

    @Column(name = "image_file_name")
    private String imageFileName;

    public Product() {
    }

    // Getters and setters

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getVendorId() { return vendorId; }
    public void setVendorId(Integer vendorId) { this.vendorId = vendorId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public java.math.BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(java.math.BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getProductStatus() { return productStatus; }
    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Boolean getReturnable() { return returnable; }
    public void setReturnable(Boolean returnable) {
        this.returnable = returnable;
    }

    public Boolean getReplaceable() { return replaceable; }
    public void setReplaceable(Boolean replaceable) {
        this.replaceable = replaceable;
    }

    public Integer getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(Integer warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    public String getImageFileName() { return imageFileName; }
    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    @Transient
    public Double getDiscountedPrice() {
        if (price == null || discountPercentage == null) {
            return price;
        }
        return price - (price * discountPercentage / 100.0);
    }

    @Transient
    public Double getSavings() {
        if (price == null || discountPercentage == null) {
            return 0.0;
        }
        return price * discountPercentage / 100.0;
    }

    @Transient
    public boolean hasDiscount() {
        return discountPercentage != null && discountPercentage > 0;
    }
}
