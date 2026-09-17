package com.useandget.repository;

import com.useandget.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SegmentRepository extends JpaRepository<Customer, Integer> {
}
