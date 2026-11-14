package com.vendor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vendor.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    Optional<Order> findByOrderNumber(String orderNumber);
}