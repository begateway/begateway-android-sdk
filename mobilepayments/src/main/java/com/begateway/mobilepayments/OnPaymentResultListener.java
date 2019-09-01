package com.begateway.mobilepayments;

import com.begateway.mobilepayments.model.PaymentResultResponse;

public interface OnPaymentResultListener {

    void onPaymentResult(PaymentResultResponse paymentResult);
}
