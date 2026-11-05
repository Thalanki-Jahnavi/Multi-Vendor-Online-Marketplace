
package com.vendor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vendor.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Check whether an email already exists
    boolean existsByEmail(String email);

    // Find a user by email for login authentication
    Optional<User> findByEmail(String email);

    // Retrieve all users with a specific role
    List<User> findByRoleIgnoreCase(String role);
}
