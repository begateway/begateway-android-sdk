package com.begateway.mobilepayments;


public enum TransactionType {

    PAYMENT("PAYMENT", 0),
    AUTHORIZATION("AUTHORIZATION", 1),
    VERIFY("VERIFY", 2);

    private String stringValue;
    private int intValue;
    private TransactionType(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    @Override
    public String toString() {
        return stringValue;
    }

}