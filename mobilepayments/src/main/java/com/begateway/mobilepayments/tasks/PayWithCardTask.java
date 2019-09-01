package com.begateway.mobilepayments.tasks;

import com.begateway.mobilepayments.model.BaseResponse;
import com.begateway.mobilepayments.model.PaymentResultResponse;
import com.begateway.mobilepayments.model.ResponseCode;

import org.json.JSONException;
import org.json.JSONObject;

public class PayWithCardTask extends BaseRequestTask<PaymentResultResponse> {

    private IPayWithCardTaskCallback callback;

    public PayWithCardTask setCallback(IPayWithCardTaskCallback callback) {
        this.callback = callback;

        return this;
    }

    public PayWithCardTask fillBodyRequest(String paymentToken, String cardNumber, String cvv, String cardHolder, String cardExpMonth, String cardExpYear) {

        JSONObject rootJson = new JSONObject();

        try {

            JSONObject requestJson = new JSONObject();
            JSONObject creditCardJson = new JSONObject();

            if (cardNumber.isEmpty() == false){
                creditCardJson.put("number", cardNumber);
            }
            if (cvv.isEmpty() == false){
                creditCardJson.put("verification_value", cvv);
            }

            if (cardHolder.isEmpty() == false){
                creditCardJson.put("holder", cardHolder);
            }

            if (cardExpMonth.isEmpty() == false){
                creditCardJson.put("exp_month", cardExpMonth);
            }

            if (cardExpYear.isEmpty() == false){
                creditCardJson.put("exp_year", cardExpYear);
            }

            requestJson.put("token", paymentToken);
            requestJson.put("payment_method", "credit_card");
            requestJson.put("credit_card", creditCardJson);

            rootJson.put("request", requestJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setJsonBody(rootJson.toString());

        return this;
    }

    @Override
    public BaseResponse getResponseInstance() {
        return new PaymentResultResponse();
    }

    @Override
    protected void onPostExecute(BaseResponse response) {
        super.onPostExecute(response);

        PaymentResultResponse targetResponse = (PaymentResultResponse)response;

        if (response.getStatus() == ResponseCode.SUCCESS) {
            try {

                JSONObject jsonData = response.getRawJson();
                JSONObject responseObject = jsonData.getJSONObject("response");
                JSONObject creditCardObject = responseObject.getJSONObject("credit_card");

                targetResponse.setToken(responseObject.getString("token"));
                targetResponse.setPaymentStatus(responseObject.getString("status"));
                targetResponse.setTokenCard(creditCardObject.getString("token"));

                targetResponse.setUrl(responseObject.getString("url"));

            } catch (JSONException e) {
                response.setError(e.toString());
            }
        }

        if (callback != null)
        {
            callback.onCallback(targetResponse);
        }
    }
}
