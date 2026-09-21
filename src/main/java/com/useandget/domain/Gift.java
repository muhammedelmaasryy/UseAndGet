package com.useandget.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "gift")
public class Gift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gift_id")
    private Integer giftId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gift_type", nullable = false)
    private ConsumptionType giftType;

    @Column(name = "gift_amount", nullable = false)
    private Integer giftAmount;

    public Integer getGiftId() {
        return giftId;
    }

    public void setGiftId(Integer giftId) {
        this.giftId = giftId;
    }

    public ConsumptionType getGiftType() {
        return giftType;
    }

    public void setGiftType(ConsumptionType giftType) {
        this.giftType = giftType;
    }

    public Integer getGiftAmount() {
        return giftAmount;
    }

    public void setGiftAmount(Integer giftAmount) {
        this.giftAmount = giftAmount;
    }
}
