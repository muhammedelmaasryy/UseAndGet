package com.useandget.dto;


public class EvaluationResult {

    private Integer offerId;
    private String phoneNumber;
    private String previousState;
    private String newState;
    private Double consumedAmount;
    private Double threshold;
    private String message;

    public EvaluationResult() {}

    public EvaluationResult(Integer offerId, String phoneNumber, String previousState,
                            String newState, Double consumedAmount, Double threshold, String message) {
        this.offerId = offerId;
        this.phoneNumber = phoneNumber;
        this.previousState = previousState;
        this.newState = newState;
        this.consumedAmount = consumedAmount;
        this.threshold = threshold;
        this.message = message;
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

    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public Double getConsumedAmount() {
        return consumedAmount;
    }

    public void setConsumedAmount(Double consumedAmount) {
        this.consumedAmount = consumedAmount;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
