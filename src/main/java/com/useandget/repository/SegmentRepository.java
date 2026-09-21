package com.useandget.repository;

import com.useandget.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SegmentRepository extends JpaRepository<Segment, Integer> {

    Optional<Segment> findByNameIgnoreCase(String name);
}
