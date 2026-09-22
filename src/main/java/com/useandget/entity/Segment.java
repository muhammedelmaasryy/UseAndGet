package com.useandget.entity;

import jakarta.persistence.*;

@Entity
public class Segment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer segmentId;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsumptionType consumptionType;
    @Column(nullable = false)
    private Integer threshold;
    @Column(nullable = false)
    private Integer maxRetries;
    @Column(nullable = false)
    private Integer cooldownDays;

    @ManyToOne
    @JoinColumn(name="gift_id",nullable = false)
    private Gift gift;

    public Segment(Integer segmentId, String name, ConsumptionType consumptionType, Integer threshold, Integer maxRetries, Integer cooldownDays, Gift gift) {
        this.segmentId = segmentId;
        this.name = name;
        this.consumptionType = consumptionType;
        this.threshold = threshold;
        this.maxRetries = maxRetries;
        this.cooldownDays = cooldownDays;
        this.gift = gift;
    }

    public Segment() {}

    public Integer getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(Integer segmentId) {
        this.segmentId = segmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConsumptionType getConsumptionType() {
        return consumptionType;
    }

    public void setConsumptionType(ConsumptionType consumptionType) {
        this.consumptionType = consumptionType;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getCooldownDays() {
        return cooldownDays;
    }

    public void setCooldownDays(Integer cooldownDays) {
        this.cooldownDays = cooldownDays;
    }

    public Gift getGift() {
        return gift;
    }

    public void setGift(Gift gift) {
        this.gift = gift;
    }
}
