
package com.vendor.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.vendor.service.StoreImageStorageService;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StoreImageStorageService storageService;

    public WebConfig(StoreImageStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path uploadRoot = storageService.getUploadRoot();

        String uploadLocation = uploadRoot.toUri().toString();

        registry.addResourceHandler("/store-uploads/**")
                .addResourceLocations(uploadLocation);
    }
}
