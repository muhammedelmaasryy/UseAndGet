package com.useandget.dto;

public class ConsumptionRow {

    private String phoneNumber;
    private Double consumedAmount;

    public ConsumptionRow() {}

    public ConsumptionRow(String phoneNumber, Double consumedAmount) {
        this.phoneNumber = phoneNumber;
        this.consumedAmount = consumedAmount;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Double getConsumedAmount() {
        return consumedAmount;
    }

    public void setConsumedAmount(Double consumedAmount) {
        this.consumedAmount = consumedAmount;
    }
}
