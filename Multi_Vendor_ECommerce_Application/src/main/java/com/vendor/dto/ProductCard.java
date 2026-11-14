package com.vendor.dto;

import java.math.BigDecimal;

public record ProductCard(
        Integer productId,
        String productName,
        BigDecimal price,
        BigDecimal discountPercentage,
        String vendorName
) {
}