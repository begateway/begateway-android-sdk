package com.begateway.mobilepayments.model;

public enum ResponseCode {

    SUCCESS("SUCCESS", 0),
    ERROR("ERROR", 1),
    TIMEOUT("TIMEOUT", 2),
    CANCELED("CANCELED", 3),
    INCOMPLETE("INCOMPLETE", 4),
    FAILED("FAILED", 5),
    CONNECTION_ERROR("CONNECTION_ERROR", 6);

    private String stringValue;
    private int intValue;
    private ResponseCode(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    @Override
    public String toString() {
        return stringValue;
    }

}
