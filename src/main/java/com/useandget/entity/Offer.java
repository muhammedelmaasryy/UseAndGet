package com.useandget.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "offer")
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Integer offerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "offered_at")
    private LocalDateTime offeredAt;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    @Column(name = "cooldown_until")
    private LocalDateTime cooldownUntil;

    @Column(name = "rewarded_at")
    private LocalDateTime rewardedAt;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "segment_id", nullable = false)
    private Segment segment;

    public Offer() {
    }

    public Offer(Integer offerId, State state, Integer retryCount, LocalDateTime offeredAt, LocalDateTime evaluatedAt, LocalDateTime cooldownUntil, LocalDateTime rewardedAt, Customer customer, Segment segment) {
        this.offerId = offerId;
        this.state = state;
        this.retryCount = retryCount;
        this.offeredAt = offeredAt;
        this.evaluatedAt = evaluatedAt;
        this.cooldownUntil = cooldownUntil;
        this.rewardedAt = rewardedAt;
        this.customer = customer;
        this.segment = segment;
    }

    public Integer getOfferId() {
        return offerId;
    }

    public void setOfferId(Integer offerId) {
        this.offerId = offerId;
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }
}
