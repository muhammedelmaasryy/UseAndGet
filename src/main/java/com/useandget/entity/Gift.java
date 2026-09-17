package com.useandget.entity;

import jakarta.persistence.*;

@Entity
public class Gift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer giftId;
    @Column(nullable = false)
    private String giftType;
    @Column(nullable = false)
    private Integer giftAmount;
}
