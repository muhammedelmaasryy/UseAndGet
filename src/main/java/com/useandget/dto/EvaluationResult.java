package com.useandget.dto;

import com.useandget.entity.State;

/**
 * Per-offer result of evaluating a consumption row against its segment's
 * threshold. Returned by EvaluationService for each matched offer.
 */
public class EvaluationResult {

    private Integer offerId;
    private State previousState;
    private State newState;
    private Double consumedAmount;
    private Integer threshold;

    public EvaluationResult() {}

    public EvaluationResult(Integer offerId, State previousState, State newState,
                             Double consumedAmount, Integer threshold) {
        this.offerId = offerId;
        this.previousState = previousState;
        this.newState = newState;
        this.consumedAmount = consumedAmount;
        this.threshold = threshold;
    }

    public Integer getOfferId() {
        return offerId;
    }

    public void setOfferId(Integer offerId) {
        this.offerId = offerId;
    }

    public State getPreviousState() {
        return previousState;
    }

    public void setPreviousState(State previousState) {
        this.previousState = previousState;
    }

    public State getNewState() {
        return newState;
    }

    public void setNewState(State newState) {
        this.newState = newState;
    }

    public Double getConsumedAmount() {
        return consumedAmount;
    }

    public void setConsumedAmount(Double consumedAmount) {
        this.consumedAmount = consumedAmount;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }
}
