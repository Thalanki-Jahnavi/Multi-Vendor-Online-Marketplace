package com.vendor.service;

import com.vendor.dto.VendorDTO;
import com.vendor.entity.User;
import com.vendor.repository.VendorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public List<VendorDTO> getVendors() {
        return vendorRepository.findByRole("vendor")
                .stream()
                .map(this::toVendorDTO)
                .toList();
    }

    private VendorDTO toVendorDTO(User user) {
        String storeName = user.getBusinessName();

        if (storeName == null || storeName.isBlank()) {
            storeName = user.getFirstName() + " " + user.getLastName();
        }

        return new VendorDTO(user.getUserId(), storeName);
    }
}