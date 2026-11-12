
package com.vendor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.vendor.service.StoreImageStorageService;

@Configuration
public class StoreUploadWebConfig implements WebMvcConfigurer {

    private final StoreImageStorageService storageService;

    public StoreUploadWebConfig(
            StoreImageStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String location =
                storageService.getUploadRoot().toUri().toString();

        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler("/store-uploads/**")
                .addResourceLocations(location);
    }
}
