package com.begateway.mobilepayments;

import android.app.Activity;
import android.content.Context;

import com.begateway.mobilepayments.utils.CardType;

import java.util.List;

public class PaymentBuilder {

    private PaymentSettings paymentSettings;

    private OnPaymentResultListener paymentResultListener;

    public PaymentBuilder(){
        this.paymentSettings = new PaymentSettings();
    }

    public PaymentBuilder setPublicKey(String publicKey) {
        paymentSettings.setPublicKey(publicKey);

        return this;
    }

    public PaymentBuilder setSupportedCardTypes(List<CardType> supportedCardTypes) {
        paymentSettings.setSupportedCardTypes(supportedCardTypes);

        return this;
    }

    public PaymentBuilder setUseEnctyptedCard(boolean useEnctyptedCard) {
        paymentSettings.setUseEnctyptedCard(useEnctyptedCard);
        return this;
    }


    public PaymentBuilder setTestMode(boolean testMode) {
        paymentSettings.setTestMode(testMode);

        return this;
    }

    public PaymentBuilder setSecuredByLabel(String securedByLabel) {
        paymentSettings.setSecuredBy(securedByLabel);

        return this;
    }

    public PaymentBuilder setLocale(String locale) {
        paymentSettings.setLocale(locale);

        return this;
    }

    public PaymentBuilder setEndpoint(String endpoint) {
        paymentSettings.setEndpoint(endpoint);

        return this;
    }

    public PaymentBuilder setPaymentResultListener(OnPaymentResultListener paymentResultListener) {
        this.paymentResultListener = paymentResultListener;

        return this;
    }

    public PaymentBuilder setPaymentTestData(PaymentTestData paymentTestData) {
        paymentSettings.setPaymentTestData(paymentTestData);

        return this;
    }

    public PaymentBuilder setStyleSettings(StyleSettings styleSettings) {
        paymentSettings.setStyleSettings(styleSettings);

        return this;
    }

    public PaymentBuilder setReturnUrl(String returnUrl) {
        paymentSettings.setReturnUrl(returnUrl);

        return this;
    }

    public PaymentModule build(Context context, Activity activity){

        PaymentModule paymentModule = PaymentModule.getInstance();
        paymentModule.setPaymentResultListener(paymentResultListener);
        paymentModule.setup(paymentSettings, context, activity);

        return paymentModule;
    }


}
