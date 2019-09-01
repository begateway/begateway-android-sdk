package com.begateway.mobilepayments.model;

public enum ResponseCode {

    SUCCESS("SUCCESS", 0),
    ERROR("ERROR", 1),
    TIMEOUT("TIMEOUT", 2),
    CANCELED("CANCELED", 3);

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
