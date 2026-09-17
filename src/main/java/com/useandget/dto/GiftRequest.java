package com.useandget.dto;

/**
 * Request DTO for creating or updating a Gift via the REST API.
 */
public class GiftRequest {

    private String giftType;
    private Integer giftAmount;

    public GiftRequest() {}

    public GiftRequest(String giftType, Integer giftAmount) {
        this.giftType = giftType;
        this.giftAmount = giftAmount;
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
}
