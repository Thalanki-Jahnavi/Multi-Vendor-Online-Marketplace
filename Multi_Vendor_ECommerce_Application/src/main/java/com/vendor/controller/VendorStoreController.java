
package com.vendor.controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vendor.entity.User;
import com.vendor.repository.UserRepository;
import com.vendor.service.StoreImageStorageService;

@Controller
public class VendorStoreController {

    private final UserRepository userRepository;
    private final StoreImageStorageService storageService;

    public VendorStoreController(
            UserRepository userRepository,
            StoreImageStorageService storageService) {

        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    // Display My Store page
    @GetMapping("/vendor/store")
    public String showStorePage(
            Authentication authentication,
            Model model) {

        Optional<User> vendorOptional =
                getLoggedInVendor(authentication);

        if (vendorOptional.isEmpty()) {
            return "redirect:/login";
        }

        User vendor = vendorOptional.get();

        model.addAttribute("vendor", vendor);
        model.addAttribute("vendorName", vendor.getBusinessName());

        return "vendor/store";
    }

    // Update store name, logo and banner
    @PostMapping(
        value = "/vendor/store/update",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String updateStore(
            Authentication authentication,

            @RequestParam(required = false)
            String businessName,

            @RequestParam(required = false)
            MultipartFile logoFile,

            @RequestParam(required = false)
            MultipartFile bannerFile,

            RedirectAttributes redirectAttributes) {

        Optional<User> vendorOptional =
                getLoggedInVendor(authentication);

        if (vendorOptional.isEmpty()) {
            return "redirect:/login";
        }

        String cleanedName =
                businessName == null ? "" : businessName.trim();

        if (cleanedName.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Store name is required."
            );
            return "redirect:/vendor/store";
        }

        if (cleanedName.length() > 255) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Store name must be 255 characters or fewer."
            );
            return "redirect:/vendor/store";
        }

        User vendor = vendorOptional.get();

        String oldLogo = vendor.getStoreLogo();
        String oldBanner = vendor.getStoreBanner();

        Integer vendorId = vendor.getUserId();

        try {

            String newLogo = null;
            String newBanner = null;

            // Save logo if selected
            if (logoFile != null && !logoFile.isEmpty()) {

                newLogo = storageService.saveImage(
                        logoFile,
                        vendorId,
                        "logo",
                        2L * 1024 * 1024
                );
            }

            // Save banner if selected
            if (bannerFile != null && !bannerFile.isEmpty()) {

                newBanner = storageService.saveImage(
                        bannerFile,
                        vendorId,
                        "banner",
                        5L * 1024 * 1024
                );
            }

            // Update store name
            vendor.setBusinessName(cleanedName);

            // Update image paths only when new files were selected
            if (newLogo != null) {
                vendor.setStoreLogo(newLogo);
            }

            if (newBanner != null) {
                vendor.setStoreBanner(newBanner);
            }

            // Save database changes
            userRepository.save(vendor);

            // Delete old images only after saving the new paths
            if (newLogo != null) {
                storageService.deletePreviousImage(oldLogo, vendorId);
            }

            if (newBanner != null) {
                storageService.deletePreviousImage(oldBanner, vendorId);
            }

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Your store details and images were updated successfully."
            );

        } catch (IOException | IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Unable to upload store images."
            );
        }

        return "redirect:/vendor/store";
    }

    // Get currently logged-in vendor
    private Optional<User> getLoggedInVendor(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            return Optional.empty();
        }

        Optional<User> userOptional =
                userRepository.findByEmail(authentication.getName());

        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();

        if (user.getRole() == null
                || !"vendor".equalsIgnoreCase(user.getRole())) {

            return Optional.empty();
        }

        return Optional.of(user);
    }
}
