
package com.vendor.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vendor.entity.User;
import com.vendor.repository.UserRepository;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Display Login Page
    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String registered,
            Model model) {

        if (error != null) {
            model.addAttribute(
                    "error",
                    "Invalid email or password. Please try again."
            );
        }

        if (logout != null) {
            model.addAttribute(
                    "success",
                    "You have been logged out successfully."
            );
        }

        if (registered != null) {
            model.addAttribute(
                    "success",
                    "Registration successful. Please log in."
            );
        }

        return "public/login";
    }

    // Register Customer or Vendor
    @PostMapping("/signup")
    public String signup(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String role,

            @RequestParam(required = false) String businessName,
            @RequestParam(required = false) String gstNumber,

            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String addressLine,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String postalCode,

            @RequestParam String password,
            @RequestParam String confirmPassword,

            Model model) {

        email = email.trim().toLowerCase();
        role = role.trim().toLowerCase();

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "public/signup";
        }

        if (password.length() < 8) {
            model.addAttribute(
                    "error",
                    "Password must be at least 8 characters."
            );
            return "public/signup";
        }

        if (!role.equals("customer") && !role.equals("vendor")) {
            model.addAttribute(
                    "error",
                    "Choose a valid account type."
            );
            return "public/signup";
        }

        if (role.equals("vendor")
                && (businessName == null
                || businessName.trim().isEmpty())) {

            model.addAttribute(
                    "error",
                    "Business name is required for vendor accounts."
            );

            return "public/signup";
        }

        if (userRepository.existsByEmail(email)) {
            model.addAttribute(
                    "error",
                    "An account with this email already exists."
            );

            return "public/signup";
        }

        System.out.println("========== SIGNUP DEBUG ==========");
        System.out.println("Phone: " + phone);
        System.out.println("GST: " + gstNumber);
        System.out.println("Address: " + addressLine);
        System.out.println("City: " + city);
        System.out.println("State: " + state);
        System.out.println("Postal Code: " + postalCode);
        System.out.println("==================================");

        User user = new User();

        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email);
        user.setRole(role);

        user.setPassword(passwordEncoder.encode(password));

        user.setBusinessName(
                role.equals("vendor") ? businessName.trim() : null
        );

        user.setGstNumber(
                role.equals("vendor")
                        && gstNumber != null
                        && !gstNumber.isBlank()
                        ? gstNumber.trim().toUpperCase()
                        : null
        );

        user.setPhone(clean(phone));
        user.setAddressLine(clean(addressLine));
        user.setCity(clean(city));
        user.setState(clean(state));
        user.setPostalCode(clean(postalCode));

        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        userRepository.save(user);

        return "redirect:/login?registered";
    }

    private String clean(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
