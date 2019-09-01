package com.begateway.mobilepayments.tasks;

import com.begateway.mobilepayments.model.BaseResponse;
import com.begateway.mobilepayments.model.PaymentTokenResponse;
import com.begateway.mobilepayments.model.ResponseCode;

import org.json.JSONException;
import org.json.JSONObject;

public class RetrievePaymentTokenTask extends BaseRequestTask<PaymentTokenResponse> {

    private String templateBody = "{\n" +
            "    \"checkout\": {\n" +
            "        \"test\": %s,\n" +
            "        \"transaction_type\": \"payment\",\n" +
            "        \"attempts\": %s,\n" +
            "        \"order\": {\n" +
            "            \"amount\": \"%s\",\n" +
            "            \"currency\": \"%s\",\n" +
            "            \"description\": \"%s\",\n" +
            "            \"additional_data\": {\n"+
            "               \"contract\": [ \"recurring\", \"card_on_file\"]\n"+
            "               }\n"+
            "        }\n" +
            "    }\n" +
            "}";

    private IRetrievePaymentTokenTask callback;

    public RetrievePaymentTokenTask setCallback(IRetrievePaymentTokenTask callback) {
        this.callback = callback;

        return this;
    }

    public RetrievePaymentTokenTask fillBodyRequest(boolean isTestMode, String attempts, String orderAmount, String orderCurrency, String orderDescription) {

        String result = String.format(templateBody, isTestMode, attempts, orderAmount, orderCurrency, orderDescription);

        setJsonBody(result);

        return this;
    }

    @Override
    public BaseResponse getResponseInstance() {
        return new PaymentTokenResponse();
    }

    @Override
    protected void onPostExecute(BaseResponse response) {
        super.onPostExecute(response);

        PaymentTokenResponse targetResponse = (PaymentTokenResponse)response;

        if (response.getStatus() == ResponseCode.SUCCESS) {
            try {

                JSONObject jsonData = response.getRawJson();
                targetResponse.Fill(jsonData);

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