package com.vendor.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @PostMapping("/signup")
    public String signup(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam(required = false) String businessName,
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
            model.addAttribute("error", "Password must be at least 8 characters.");
            return "public/signup";
        }

        if (!role.equals("customer") && !role.equals("vendor")) {
            model.addAttribute("error", "Choose a valid account type.");
            return "public/signup";
        }

        if (role.equals("vendor")
                && (businessName == null || businessName.trim().isEmpty())) {
            model.addAttribute("error", "Business name is required for vendor accounts.");
            return "public/signup";
        }

        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "An account with this email already exists.");
            return "public/signup";
        }

        User user = new User();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email);
        user.setRole(role);
        user.setBusinessName(role.equals("vendor") ? businessName.trim() : null);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);

        return "redirect:/login?registered";
    }
}