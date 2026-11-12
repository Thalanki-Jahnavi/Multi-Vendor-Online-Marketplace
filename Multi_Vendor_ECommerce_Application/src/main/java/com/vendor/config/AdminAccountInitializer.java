
package com.vendor.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.vendor.entity.User;
import com.vendor.repository.UserRepository;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String email = System.getenv("ONECART_ADMIN_EMAIL");
        String password = System.getenv("ONECART_ADMIN_PASSWORD");

        if (email == null || email.isBlank()
                || password == null || password.length() < 8) {
            System.out.println(
                "Admin initialization skipped. Configure admin environment variables.");
            return;
        }

        email = email.trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            System.out.println("Admin email already exists: " + email);
            return;
        }

        User admin = new User();

        admin.setFirstName("OneCart");
        admin.setLastName("Administrator");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole("admin");
        admin.setEmailVerified(true);

        userRepository.save(admin);

        System.out.println("==================================");
        System.out.println("OneCart Admin account created!");
        System.out.println("Admin email: " + email);
        System.out.println("==================================");
    }
}
