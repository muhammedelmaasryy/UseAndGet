package com.useandget.dto;

import com.useandget.entity.Offer;

/**
 * A consumption file row that was successfully matched to an active
 * (state = OFFERED) offer. Handed off from ConsumptionService to
 * EvaluationService.
 */
public class MatchedConsumption {

    private Offer offer;
    private Double consumedAmount;

    public MatchedConsumption() {}

    public MatchedConsumption(Offer offer, Double consumedAmount) {
        this.offer = offer;
        this.consumedAmount = consumedAmount;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public Double getConsumedAmount() {
        return consumedAmount;
    }

    public void setConsumedAmount(Double consumedAmount) {
        this.consumedAmount = consumedAmount;
    }
}
