package com.begateway.mobilepayments.tasks;

import com.begateway.mobilepayments.model.PaymentResultResponse;

public interface IPayWithCardTaskCallback {

    void onCallback(PaymentResultResponse response);
}
