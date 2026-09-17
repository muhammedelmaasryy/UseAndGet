package com.useandget.dto;

public class CustomerRequest {

    private String phoneNumber;
    private String name;
    private Integer segmentId;

    public CustomerRequest() {}

    public CustomerRequest(String phoneNumber, String name, Integer segmentId) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.segmentId = segmentId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(Integer segmentId) {
        this.segmentId = segmentId;
    }
}
