
package com.vendor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                // Public pages and static resources
                .requestMatchers(
                    "/",
                    "/about",
                    "/contact",
                    "/login",
                    "/signup",
                    "/forgot-password",
                    "/reset-password",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/store-uploads/**"
                ).permitAll()

                // Protected pages by role
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .requestMatchers("/vendor/**").hasRole("VENDOR")
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Customer-only actions
                .requestMatchers(
                    "/products",
                    "/cart/**",
                    "/wishlist/**",
                    "/checkout/**"
                ).hasRole("CUSTOMER")

                // Everything else requires login
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {

        return (request, response, authentication) -> {

            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                    authority.getAuthority().equals("ROLE_ADMIN"));

            boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                    authority.getAuthority().equals("ROLE_VENDOR"));

            if (isAdmin) {
                response.sendRedirect("/admin/dashboard");
            } else if (isVendor) {
                response.sendRedirect("/vendor/dashboard");
            } else {
                // Redirect customers directly to Shop Products
                response.sendRedirect("/customer/products");
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
