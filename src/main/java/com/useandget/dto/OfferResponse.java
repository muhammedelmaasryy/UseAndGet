package com.useandget.dto;

import com.useandget.entity.State;

import java.time.LocalDateTime;


public class OfferResponse {

    private Integer offerId;
    private String phoneNumber;
    private String segmentName;
    private State state;
    private Integer retryCount;
    private LocalDateTime offeredAt;
    private LocalDateTime evaluatedAt;
    private LocalDateTime cooldownUntil;
    private LocalDateTime rewardedAt;

    public OfferResponse() {}

    public OfferResponse(Integer offerId, String phoneNumber, String segmentName, State state,
                         Integer retryCount, LocalDateTime offeredAt, LocalDateTime evaluatedAt,
                         LocalDateTime cooldownUntil, LocalDateTime rewardedAt) {
        this.offerId = offerId;
        this.phoneNumber = phoneNumber;
        this.segmentName = segmentName;
        this.state = state;
        this.retryCount = retryCount;
        this.offeredAt = offeredAt;
        this.evaluatedAt = evaluatedAt;
        this.cooldownUntil = cooldownUntil;
        this.rewardedAt = rewardedAt;
    }

    public Integer getOfferId() {
        return offerId;
    }

    public void setOfferId(Integer offerId) {
        this.offerId = offerId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSegmentName() {
        return segmentName;
    }

    public void setSegmentName(String segmentName) {
        this.segmentName = segmentName;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getOfferedAt() {
        return offeredAt;
    }

    public void setOfferedAt(LocalDateTime offeredAt) {
        this.offeredAt = offeredAt;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public LocalDateTime getCooldownUntil() {
        return cooldownUntil;
    }

    public void setCooldownUntil(LocalDateTime cooldownUntil) {
        this.cooldownUntil = cooldownUntil;
    }

    public LocalDateTime getRewardedAt() {
        return rewardedAt;
    }

    public void setRewardedAt(LocalDateTime rewardedAt) {
        this.rewardedAt = rewardedAt;
    }
}
