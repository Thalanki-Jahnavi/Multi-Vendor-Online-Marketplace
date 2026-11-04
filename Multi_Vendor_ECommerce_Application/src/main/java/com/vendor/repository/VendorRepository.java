package com.vendor.repository;

import com.vendor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorRepository extends JpaRepository<User, Long> {

    List<User> findByRole(String role);
}