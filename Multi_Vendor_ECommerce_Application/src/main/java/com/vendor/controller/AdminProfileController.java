
package com.vendor.controller;

import com.vendor.entity.User;
import com.vendor.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminProfileController {

    private final UserRepository userRepository;

    public AdminProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/admin/profile")
    public String showProfile(Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User admin = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/";
        }

        model.addAttribute("admin", admin);
        model.addAttribute("activePage", "profile");

        return "admin/profile";
    }

    @PostMapping("/admin/profile/update")
    public String updateProfile(
            Authentication authentication,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phone,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User admin = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/";
        }

        firstName = firstName.trim();
        lastName = lastName.trim();
        email = email.trim();
        phone = phone.trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "First name, last name and email are required.");
            return "redirect:/admin/profile";
        }

        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null &&
                !existingUser.getUserId().equals(admin.getUserId())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "This email address is already registered.");
            return "redirect:/admin/profile";
        }

        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setEmail(email);
        admin.setPhone(phone);

        userRepository.save(admin);

        // Update the authenticated username if the email changed.
        if (!authentication.getName().equals(email)) {
            UsernamePasswordAuthenticationToken newAuth =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authentication.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        redirectAttributes.addFlashAttribute(
                "successMessage", "Profile updated successfully!");

        return "redirect:/admin/profile";
    }
}
