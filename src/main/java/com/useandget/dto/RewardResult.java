package com.useandget.dto;


public class RewardResult {

    private Integer offerId;
    private String phoneNumber;
    private String giftType;
    private Integer giftAmount;
    private String message;

    public RewardResult() {}

    public RewardResult(Integer offerId, String phoneNumber, String giftType,
                        Integer giftAmount, String message) {
        this.offerId = offerId;
        this.phoneNumber = phoneNumber;
        this.giftType = giftType;
        this.giftAmount = giftAmount;
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

    public String getGiftType() {
        return giftType;
    }

    public void setGiftType(String giftType) {
        this.giftType = giftType;
    }

    public Integer getGiftAmount() {
        return giftAmount;
    }

    public void setGiftAmount(Integer giftAmount) {
        this.giftAmount = giftAmount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
