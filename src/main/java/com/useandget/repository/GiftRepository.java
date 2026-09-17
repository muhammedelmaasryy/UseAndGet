package com.useandget.repository;

import com.useandget.entity.Gift;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiftRepository extends JpaRepository<Gift, Integer> {
}
