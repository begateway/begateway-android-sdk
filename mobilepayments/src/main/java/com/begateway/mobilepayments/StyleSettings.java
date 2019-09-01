package com.begateway.mobilepayments;

public class StyleSettings {

    private boolean isRequiredCardHolderName = true;

    private boolean isRequiredCardNumber = true;

    private boolean isRequiredCVV = true;

    private boolean isRequiredExpDate = true;

    private boolean isMaskCVV = true;

    private boolean isMaskCardNumber = false;

    private boolean saveCardCheckboxDefaultState = true;

    private boolean isSaveCardCheckBoxVisible = true;

    private boolean isScanCardVisible = true;

    private boolean isSecuredLabelVisible = true;

    private String customPayButtonLabel;


    public boolean isSecuredLabelVisible() {
        return isSecuredLabelVisible;
    }

    public StyleSettings setSecuredLabelVisible(boolean securedLabelVisible) {
        isSecuredLabelVisible = securedLabelVisible;

        return this;
    }

    public boolean isScanCardVisible() {
        return isScanCardVisible;
    }

    public StyleSettings setScanCardVisible(boolean scanCardVisible) {
        isScanCardVisible = scanCardVisible;

        return this;
    }

    public String getCustomPayButtonLabel() {
        return customPayButtonLabel;
    }

    public StyleSettings setCustomPayButtonLabel(String customPayButtonLabel) {
        this.customPayButtonLabel = customPayButtonLabel;

        return this;
    }

    public boolean isSaveCardCheckBoxVisible() {
        return isSaveCardCheckBoxVisible;
    }

    public StyleSettings setSaveCardCheckBoxVisible(boolean saveCardCheckBoxVisible) {
        isSaveCardCheckBoxVisible = saveCardCheckBoxVisible;

        return this;
    }

    public boolean isSaveCardCheckboxDefaultState() {
        return saveCardCheckboxDefaultState;
    }

    public StyleSettings setSaveCardCheckboxDefaultState(boolean saveCardCheckboxDefaultState) {
        this.saveCardCheckboxDefaultState = saveCardCheckboxDefaultState;

        return this;
    }

    public boolean isMaskCardNumber() {
        return isMaskCardNumber;
    }

    public StyleSettings setMaskCardNumber(boolean maskCardNumber) {
        isMaskCardNumber = maskCardNumber;

        return this;
    }

    public boolean isMaskCVV() {
        return isMaskCVV;
    }

    public StyleSettings setMaskCVV(boolean maskCVV) {
        isMaskCVV = maskCVV;

        return this;
    }

    public StyleSettings setRequiredCardHolderName(boolean requiredCardHolderName) {
        isRequiredCardHolderName = requiredCardHolderName;

        return this;
    }

    public boolean isRequiredCardHolderName() {
        return isRequiredCardHolderName;
    }

    public boolean isRequiredCardNumber() {
        return isRequiredCardNumber;
    }

    public StyleSettings setRequiredCardNumber(boolean requiredCardNumber) {
        isRequiredCardNumber = requiredCardNumber;

        return this;
    }

    public boolean isRequiredCVV() {
        return isRequiredCVV;
    }

    public StyleSettings setRequiredCVV(boolean requiredCVV) {
        isRequiredCVV = requiredCVV;

        return this;
    }

    public boolean isRequiredExpDate() {
        return isRequiredExpDate;
    }

    public StyleSettings setRequiredExpDate(boolean requiredExpDate) {
        isRequiredExpDate = requiredExpDate;

        return this;
    }
}
