package com.begateway.mobilepayments;


import com.begateway.mobilepayments.utils.CardType;

import java.util.List;

public class PaymentSettings {

    private String endpoint;

    private String returnUrl = "https://DEFAULT_RETURN_URL.com";

    private String notificationUrl = "";

    private TransactionType transactionType = TransactionType.PAYMENT;

    private boolean isTestMode;

    private boolean isUseEnctyptedCard;

    private String locale = "en";

    private String securedBy = "beGateway";

    private PaymentTestData paymentTestData;

    private List<CardType> supportedCardTypes;

    private StyleSettings styleSettings = new StyleSettings();

    private String publicKey;

    private boolean isDebugMode;

    private int attempts = 1;

    public StyleSettings getStyleSettings() {
        return styleSettings;
    }

    public void setStyleSettings(StyleSettings styleSettings) {
        this.styleSettings = styleSettings;
    }

    public PaymentTestData getPaymentTestData() {
        return paymentTestData;
    }

    public void setPaymentTestData(PaymentTestData paymentTestData) {
        this.paymentTestData = paymentTestData;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    public void setSecuredBy(String securedBy) {
        this.securedBy = securedBy;
    }

    public String getSecuredBy() {
        return securedBy;
    }

    public boolean isTestMode() {
        return isTestMode;
    }

    public void setTestMode(boolean testMode) {
        isTestMode = testMode;
    }

    public List<CardType> getSupportedCardTypes() {
        return supportedCardTypes;
    }

    public void setSupportedCardTypes(List<CardType> supportedCardTypes) {
        this.supportedCardTypes = supportedCardTypes;
    }

    public void setUseEnctyptedCard(boolean useEnctyptedCard) {
        isUseEnctyptedCard = useEnctyptedCard;
    }

    public boolean isUseEnctyptedCard() {
        return isUseEnctyptedCard;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public void setDebugMode(boolean debugMode) {
        isDebugMode = debugMode;
    }
}
