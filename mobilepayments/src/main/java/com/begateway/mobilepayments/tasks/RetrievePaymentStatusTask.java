package com.begateway.mobilepayments.tasks;

import com.begateway.mobilepayments.model.BaseResponse;
import com.begateway.mobilepayments.model.PaymentResultResponse;
import com.begateway.mobilepayments.model.ResponseCode;

import org.json.JSONException;
import org.json.JSONObject;

public class RetrievePaymentStatusTask extends BaseRequestTask<PaymentResultResponse> {

    private String templateBody = "https://checkout.bepaid.by/ctp/api/checkouts/%s";

    private IPayWithCardTaskCallback callback;

    public RetrievePaymentStatusTask setCallback(IPayWithCardTaskCallback callback) {
        this.callback = callback;

        return this;
    }

    public RetrievePaymentStatusTask fillBodyRequest(String paymentToken) {

        String result = String.format(templateBody, paymentToken);

        setEndpoint(result);

        return this;
    }

    @Override
    public BaseResponse getResponseInstance() {
        return new PaymentResultResponse();
    }

    @Override
    protected void onPostExecute(BaseResponse response) {
        super.onPostExecute(response);

        PaymentResultResponse targetResponse = (PaymentResultResponse) response;

        if (response.getStatus() == ResponseCode.SUCCESS) {
            try {

                JSONObject jsonData = response.getRawJson();
                JSONObject responseObject = jsonData.getJSONObject("checkout");

                targetResponse.setToken(responseObject.getString("token"));
                targetResponse.setPaymentStatus(responseObject.getString("status"));

            } catch (JSONException e) {
                response.setError(e.toString());
            }
        }

        if (callback != null) {
            callback.onCallback(targetResponse);
        }
    }
}
