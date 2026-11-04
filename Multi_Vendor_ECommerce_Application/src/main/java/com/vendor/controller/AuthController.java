package com.vendor.controller;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String register(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String role,
            @RequestParam(required = false) String businessName,
            Model model,
            RedirectAttributes redirectAttributes) {

        firstName = firstName.trim();
        lastName = lastName.trim();
        email = email.trim().toLowerCase(Locale.ROOT);
        role = role.trim().toUpperCase(Locale.ROOT);

        if (firstName.isBlank() || lastName.isBlank()
                || email.isBlank() || password.isBlank()) {
            model.addAttribute("error", "Please complete all required fields.");
            return "signup";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "signup";
        }

        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            return "signup";
        }

        if (!role.equals("CUSTOMER") && !role.equals("VENDOR")) {
            model.addAttribute("error", "Choose Customer or Vendor to sign up.");
            return "signup";
        }

        if (role.equals("VENDOR")
                && (businessName == null || businessName.isBlank())) {
            model.addAttribute("error", "Business name is required for Vendor signup.");
            return "signup";
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            model.addAttribute("error", "An account with this email already exists.");
            return "signup";
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setBusinessName(
                role.equals("VENDOR") ? businessName.trim() : null
        );

        userRepository.save(user);

        redirectAttributes.addFlashAttribute(
                "success",
                "Account created. You can now sign in."
        );

        return "redirect:/login";
    }
}