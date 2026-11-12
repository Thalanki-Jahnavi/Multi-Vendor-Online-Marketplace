
package com.vendor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vendor.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Find user by email
    Optional<User> findByEmail(String email);

    // Check whether email already exists
    boolean existsByEmail(String email);

    // Find users by role (ADMIN, CUSTOMER, VENDOR)
    List<User> findByRoleIgnoreCase(String role);

    // Find users using multiple user IDs
    List<User> findByUserIdIn(List<Integer> userIds);
}
