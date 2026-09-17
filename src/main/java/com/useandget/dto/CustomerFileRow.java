package com.useandget.dto;


public class CustomerFileRow {

    private String phoneNumber;
    private String name;
    private String segmentName;

    public CustomerFileRow() {}

    public CustomerFileRow(String phoneNumber, String name, String segmentName) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.segmentName = segmentName;
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

    public String getSegmentName() {
        return segmentName;
    }

    public void setSegmentName(String segmentName) {
        this.segmentName = segmentName;
    }
}
