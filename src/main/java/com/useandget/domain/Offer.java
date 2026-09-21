package com.useandget.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "offer")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Integer offerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "segment_id", nullable = false)
    private Segment segment;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private OfferState state;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "offered_at")
    private Instant offeredAt;

    @Column(name = "evaluated_at")
    private Instant evaluatedAt;

    @Column(name = "cooldown_until")
    private Instant cooldownUntil;

    @Column(name = "rewarded_at")
    private Instant rewardedAt;

    public Integer getOfferId() {
        return offerId;
    }

    public void setOfferId(Integer offerId) {
        this.offerId = offerId;
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

    public OfferState getState() {
        return state;
    }

    public void setState(OfferState state) {
        this.state = state;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Instant getOfferedAt() {
        return offeredAt;
    }

    public void setOfferedAt(Instant offeredAt) {
        this.offeredAt = offeredAt;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public Instant getCooldownUntil() {
        return cooldownUntil;
    }

    public void setCooldownUntil(Instant cooldownUntil) {
        this.cooldownUntil = cooldownUntil;
    }

    public Instant getRewardedAt() {
        return rewardedAt;
    }

    public void setRewardedAt(Instant rewardedAt) {
        this.rewardedAt = rewardedAt;
    }
}
