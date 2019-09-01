package com.begateway.mobilepayments.tasks;

import com.begateway.mobilepayments.model.PaymentTokenResponse;

public interface IRetrievePaymentTokenTask {

    void onCallback(PaymentTokenResponse response);
}
