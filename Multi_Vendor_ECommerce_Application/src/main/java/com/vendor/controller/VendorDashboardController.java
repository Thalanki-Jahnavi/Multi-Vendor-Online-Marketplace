package com.vendor.controller;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.vendor.entity.User;
import com.vendor.repository.UserRepository;
import com.vendor.service.VendorDashboardService;
import com.vendor.service.VendorDashboardService.VendorDashboardData;

@Controller
public class VendorDashboardController {

    private final UserRepository userRepository;
    private final VendorDashboardService vendorDashboardService;

    public VendorDashboardController(
            UserRepository userRepository,
            VendorDashboardService vendorDashboardService) {

        this.userRepository = userRepository;
        this.vendorDashboardService = vendorDashboardService;
    }

    @GetMapping("/vendor/dashboard")
    public String showVendorDashboard(
            Authentication authentication,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<User> userOptional =
                userRepository.findByEmail(authentication.getName());

        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }

        User vendor = userOptional.get();

        if (!"vendor".equalsIgnoreCase(vendor.getRole())) {
            return "redirect:/login?error";
        }

        VendorDashboardData dashboard =
                vendorDashboardService.getDashboardData(vendor.getUserId());

        String vendorName = vendor.getBusinessName();

        if (vendorName == null || vendorName.isBlank()) {
            vendorName = vendor.getFirstName();
        }

        model.addAttribute("vendor", vendor);
        model.addAttribute("vendorName", vendorName);
        model.addAttribute("dashboard", dashboard);

        return "vendor/dashboard";
    }
}