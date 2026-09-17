package com.useandget.dto;

import com.useandget.entity.ConsumptionType;


public class SegmentRequest {

    private String name;
    private ConsumptionType consumptionType;
    private Integer threshold;
    private Integer giftId;
    private Integer cooldownDays;
    private Integer maxRetries;

    public SegmentRequest() {}

    public SegmentRequest(String name, ConsumptionType consumptionType, Integer threshold,
                          Integer giftId, Integer cooldownDays, Integer maxRetries) {
        this.name = name;
        this.consumptionType = consumptionType;
        this.threshold = threshold;
        this.giftId = giftId;
        this.cooldownDays = cooldownDays;
        this.maxRetries = maxRetries;
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

    public Integer getGiftId() {
        return giftId;
    }

    public void setGiftId(Integer giftId) {
        this.giftId = giftId;
    }

    public Integer getCooldownDays() {
        return cooldownDays;
    }

    public void setCooldownDays(Integer cooldownDays) {
        this.cooldownDays = cooldownDays;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
