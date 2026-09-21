package com.useandget.repository;

import com.useandget.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByPhoneNumber(String phoneNumber);
}
