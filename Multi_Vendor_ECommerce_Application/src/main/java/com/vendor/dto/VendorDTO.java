package com.vendor.dto;

public class VendorDTO {

    private Integer userId;
    private String storeName;

    public VendorDTO(Integer integer, String storeName) {
        this.userId = integer;
        this.storeName = storeName;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getStoreName() {
        return storeName;
    }
}