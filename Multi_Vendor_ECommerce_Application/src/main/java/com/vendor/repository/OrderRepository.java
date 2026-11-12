
package com.vendor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vendor.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Get orders belonging to a particular customer
    List<Order> findByCustomerIdOrderByCreatedAtDesc(
            Integer customerId
    );

    // Find an order using its order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Get all orders, newest first
    List<Order> findAllByOrderByCreatedAtDesc();

    // Get only orders containing products belonging to a vendor
    @Query(value = """
            SELECT DISTINCT co.*
            FROM customer_orders co
            INNER JOIN order_items oi
                ON oi.order_id = co.order_id
            INNER JOIN products p
                ON p.product_id = oi.product_id
            WHERE p.vendor_id = :vendorId
            ORDER BY co.created_at DESC
            """, nativeQuery = true)
    List<Order> findVendorOrders(
            @Param("vendorId") Integer vendorId
    );
}
