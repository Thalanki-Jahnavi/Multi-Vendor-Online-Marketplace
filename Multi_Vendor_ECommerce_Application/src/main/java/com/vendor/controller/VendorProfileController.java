
package com.vendor.controller;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vendor.entity.User;
import com.vendor.repository.UserRepository;

@Controller
public class VendorProfileController {

    private final UserRepository userRepository;

    public VendorProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Display vendor profile
    @GetMapping("/vendor/profile")
    public String showProfile(
            Authentication authentication,
            Model model) {

        User vendor = getLoggedInVendor(authentication);

        if (vendor == null) {
            return "redirect:/login";
        }

        model.addAttribute("vendor", vendor);

        return "vendor/profile";
    }

    // Update vendor profile
    @PostMapping("/vendor/profile/update")
    public String updateProfile(
            Authentication authentication,

            @RequestParam String firstName,
            @RequestParam String lastName,

            @RequestParam(required = false) String phone,

            @RequestParam(required = false) String businessName,
            @RequestParam(required = false) String gstNumber,

            @RequestParam(required = false) String addressLine,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String postalCode,

            RedirectAttributes redirectAttributes) {

        User vendor = getLoggedInVendor(authentication);

        if (vendor == null) {
            return "redirect:/login";
        }

        // Validate personal information
        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "First name and last name are required."
            );

            return "redirect:/vendor/profile";
        }

        // Validate business name
        if (businessName == null || businessName.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Business name is required."
            );

            return "redirect:/vendor/profile";
        }

        // Validate phone number if provided
        if (phone != null && !phone.isBlank()) {

            String cleanedPhone = phone.trim();

            if (!cleanedPhone.matches("[0-9+() -]{7,15}")) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Please enter a valid phone number."
                );

                return "redirect:/vendor/profile";
            }
        }

        // Update personal information
        vendor.setFirstName(firstName.trim());
        vendor.setLastName(lastName.trim());
        vendor.setPhone(clean(phone));

        // Update business information
        vendor.setBusinessName(businessName.trim());

        vendor.setGstNumber(
                gstNumber == null || gstNumber.isBlank()
                        ? null
                        : gstNumber.trim().toUpperCase()
        );

        // Update address information
        vendor.setAddressLine(clean(addressLine));
        vendor.setCity(clean(city));
        vendor.setState(clean(state));
        vendor.setPostalCode(clean(postalCode));

        // Save changes to MySQL
        userRepository.save(vendor);

        redirectAttributes.addFlashAttribute(
                "success",
                "Your profile has been updated successfully."
        );

        return "redirect:/vendor/profile";
    }

    // Get currently logged-in vendor
    private User getLoggedInVendor(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            return null;
        }

        Optional<User> userOptional =
                userRepository.findByEmail(authentication.getName());

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if (!"vendor".equalsIgnoreCase(user.getRole())) {
            return null;
        }

        return user;
    }

    // Clean empty values
    private String clean(String value) {

        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
