package com.useandget.entity;

import jakarta.persistence.*;

@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;
    @Column(unique = true)
    private String phoneNumber;
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "segment_id",nullable = false)
    private Segment segment;

    public Customer() {}

    public Customer(Integer customerId, String phoneNumber, String name, Segment segment) {
        this.customerId = customerId;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.segment = segment;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }
}
