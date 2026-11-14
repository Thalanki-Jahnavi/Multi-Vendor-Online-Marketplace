package com.vendor.config;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DatabaseInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create customer_orders table if missing
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customer_orders (
                    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    order_number VARCHAR(100) NOT NULL UNIQUE,
                    customer_id INT NOT NULL,
                    customer_name VARCHAR(255),
                    customer_email VARCHAR(255),
                    customer_phone VARCHAR(50),
                    shipping_address TEXT,
                    total_amount DOUBLE,
                    payment_method VARCHAR(50),
                    payment_status VARCHAR(50),
                    order_status VARCHAR(50),
                    return_reason VARCHAR(255),
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB;
            """);

            // Create order_items table if missing
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    order_id BIGINT NOT NULL,
                    product_id BIGINT,
                    product_name VARCHAR(255),
                    price DOUBLE,
                    quantity INT,
                    subtotal DOUBLE,
                    image_file_name VARCHAR(255)
                ) ENGINE=InnoDB;
            """);

            System.out.println(">>> [DatabaseInitializer] customer_orders and order_items tables verified/created successfully.");

        } catch (Exception e) {
            System.err.println(">>> [DatabaseInitializer] Table check/creation note: " + e.getMessage());
        }
    }
}
