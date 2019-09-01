package com.begateway.mobilepayments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Base64;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.begateway.mobilepayments.model.PaymentResultResponse;
import com.begateway.mobilepayments.model.PaymentTokenResponse;
import com.begateway.mobilepayments.model.ResponseCode;
import com.begateway.mobilepayments.tasks.IPayWithCardTaskCallback;
import com.begateway.mobilepayments.tasks.IRetrievePaymentTokenTask;
import com.begateway.mobilepayments.tasks.PayWithCardTask;
import com.begateway.mobilepayments.tasks.RetrievePaymentStatusTask;
import com.begateway.mobilepayments.tasks.RetrievePaymentTokenTask;
import com.begateway.mobilepayments.utils.CardType;
import com.kazakago.cryptore.CipherAlgorithm;
import com.kazakago.cryptore.Cryptore;
import com.kazakago.cryptore.DecryptResult;
import com.kazakago.cryptore.EncryptResult;
import com.kazakago.cryptore.EncryptionPadding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

public class PaymentModule implements Serializable {

    private static volatile PaymentModule sSoleInstance;

    public static PaymentModule getInstance() {
        if (sSoleInstance == null) { //if there is no instance available... create new one
            synchronized (PaymentModule.class) {
                if (sSoleInstance == null) sSoleInstance = new PaymentModule();
            }
        }
        return sSoleInstance;
    }

    //Make singleton from serialize and deserialize operation.
    protected PaymentModule readResolve() {
        return getInstance();
    }

    private Context context;

    private PaymentSettings paymentSettings;

    private AlertDialog alertDialog = null;

    private Activity activity;

    private Activity cardFormActivity;

    private OnPaymentResultListener paymentResultListener;

    private boolean isLoading = false;

    private Cryptore cryptore;



    private String publicStoreKey;

    private JSONObject orderData;

    private PaymentTokenResponse paymentTokenResponse;

    private boolean isCallbackRecieved = false;

    private PaymentModule () {

    }

    public boolean isNeedToGetPaymentToken(){
        return paymentTokenResponse == null;
    }

    PaymentTokenResponse getPaymentTokenResponse() {
        return paymentTokenResponse;
    }

    public PaymentSettings getPaymentSettings() {
        return paymentSettings;
    }

    public void setPaymentResultListener(OnPaymentResultListener paymentResultListener) {
        this.paymentResultListener = paymentResultListener;

        isCallbackRecieved = false;
    }

    public OnPaymentResultListener getPaymentResultListener() {
        return paymentResultListener;
    }

    public void setContext(Context context) {

        this.context = context;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public Context getContext() {
        return context;
    }

    void setup(PaymentSettings paymentSettings, Context context, Activity activity){

        this.paymentSettings = paymentSettings;
        this.context = context;
        this.activity = activity;

        try {
            this.cryptore = getCryptore(context, "$begatewaycsejs_1_0_0$");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Cryptore getCryptore(){

        return cryptore;
    }

    private Cryptore getCryptore(Context context, String alias) throws Exception {
        Cryptore.Builder builder = new Cryptore.Builder(alias, CipherAlgorithm.RSA);
        builder.setContext(context); // Need Only RSA on below API Lv22.
        builder.setEncryptionPadding(EncryptionPadding.RSA_PKCS1); //If Needed.
        return builder.build();
    }

    String encrypt(String plainStr) throws Exception {
        byte[] plainByte = plainStr.getBytes();
        EncryptResult result = getCryptore().encrypt(plainByte);
        return Base64.encodeToString(result.getBytes(), Base64.DEFAULT);
    }

    String decrypt(String encryptedStr) throws Exception {
        byte[] encryptedByte = Base64.decode(encryptedStr, Base64.DEFAULT);
        DecryptResult result = getCryptore().decrypt(encryptedByte, null);
        return new String(result.getBytes());
    }

    private void prepareForPayment(){

        publicStoreKey = null;
        orderData = null;
        paymentTokenResponse = null;
        isCallbackRecieved = false;

    }

    /**
     * Start payment with CREDIT_CARD_JSON
     * @param creditCardJsonData
     * Example
     * {
     *    "request":{
     *    	  "token": "a349f2aac6d45f4b165c6da02a19ad3b93c9ad89392339c32210b3ec8fe9d3a3",
     * 	  "payment_method": "credit_card",
     *       "credit_card":{
     *          "number":"4200000000000000",
     *          "verification_value":"123",
     *          "holder":"IVAN IVANOV",
     *          "exp_month":"01",
     *          "exp_year":"2020"
     *       }
     *    }
     * }
     */
    public void payWithCreditCard(JSONObject creditCardJsonData) {
        prepareForPayment();

        try {
            JSONObject requestJsonData = creditCardJsonData.getJSONObject("request");

            String token = requestJsonData.getString("token");

            payWithCardJson(activity, token, creditCardJsonData, new IPayWithCardTaskCallback() {
                @Override
                public void onCallback(PaymentResultResponse response) {
                    onPaymentComplete(response);
                }
            });

        } catch (JSONException e) {
            PaymentResultResponse paymentResultResponse = new PaymentResultResponse();
            paymentResultResponse.setError("Invalid credit card json");
            onPaymentComplete(paymentResultResponse);
        }
    }

    /**
     * Start payment with PUBLIC_STORE_KEY and ORDER_JSON
     * @param publicStoreKey
     * @param orderData
     * Example:
     * JSONObject ORDER_JSON = new JSONObject();
     * orderDataJson.put("amount", "98");
     * orderDataJson.put("currency", "USD");
     * orderDataJson.put("description", "test payment description");
     */
    public void payWithPublicKey(String publicStoreKey, JSONObject orderData) {

        prepareForPayment();

        this.publicStoreKey = publicStoreKey;
        this.orderData = orderData;

        activity.startActivity(new Intent(context, BaseCardFormActivity.class));
    }

    /**
     * Start payment with CHECKOUT_JSON
     * @param checkoutJsonData
     */
    public void payWithCheckoutJson(JSONObject checkoutJsonData) {

        prepareForPayment();

        paymentTokenResponse = new PaymentTokenResponse();
        try {
            paymentTokenResponse.Fill(checkoutJsonData);

            activity.startActivity(new Intent(context, BaseCardFormActivity.class));

        } catch (JSONException e) {
            e.printStackTrace();

            PaymentResultResponse paymentResultResponse = new PaymentResultResponse();
            paymentResultResponse.setError("Invalid paymentTokenData json");
            onPaymentComplete(paymentResultResponse);
        }

    }

    public boolean isCardSupported(CardType cardType){
        List<CardType> predefinedSupportedCardTypes = getPaymentSettings().getSupportedCardTypes();

        boolean state = true;
        if (predefinedSupportedCardTypes != null && predefinedSupportedCardTypes.size() > 0) {
            state = predefinedSupportedCardTypes.contains(cardType) == false;
        }
        else if (paymentTokenResponse != null && paymentTokenResponse.getSupportedCardTypes() != null){
            state = paymentTokenResponse.getSupportedCardTypes().contains(cardType) == false;
        }
        return state;
    }

    /**
     * Encrypt any card data (using RSA). For example cardData = 4200000000000000 to encrypted value
     * @param cardData
     * @return
     */
    public String encryptCardData(String cardData){
        try {
            String encryptedData = encrypt(cardData);

            return encryptedData;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void payWithCardInternal(final Activity activityContext, final String cardNumber, final String cardCvv, final String cardHolder, final String cardExpMonth, final String cardExpYear, final IPayWithCardTaskCallback callback) {

        if (paymentTokenResponse != null && paymentTokenResponse.getStatus() == ResponseCode.SUCCESS){

            payWithCard(activityContext, paymentTokenResponse.getToken(), cardNumber, cardCvv, cardHolder, cardExpMonth, cardExpYear, new IPayWithCardTaskCallback() {
                @Override
                public void onCallback(PaymentResultResponse response) {

                    callback.onCallback(response);
                }
            });
        }
        else {

            PaymentResultResponse paymentResultResponse = new PaymentResultResponse();
            paymentResultResponse.setError(paymentTokenResponse.getError());
            callback.onCallback(paymentResultResponse);
        }
    }

    private void onPayWithCardCallback(final Activity activityContext, final PaymentResultResponse response, final IPayWithCardTaskCallback callback, boolean isWaitForIncomplete){

        if (response.getStatus() == ResponseCode.SUCCESS) {

            if (response.getPaymentStatus().equals("incomplete")) {

                if (isWaitForIncomplete){
                    getPaymentStatus(response.getToken(), new IPayWithCardTaskCallback() {
                        @Override
                        public void onCallback(PaymentResultResponse response) {
                            onPayWithCardCallback(activityContext, response, callback, true);
                        }
                    });
                }
                else {

                    WebView webView = new WebView(activityContext);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setAllowFileAccess(true);
                    webView.loadUrl(response.getUrl());
                    webView.setWebChromeClient(new WebChromeClient() {
                    });

                    webView.setWebViewClient(new WebViewClient() {
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            return false;
                        }

                        public void onPageFinished(WebView view, String url) {

                            if (url.contains(response.getToken())) {

                                alertDialog.dismiss();

                                getPaymentStatus(response.getToken(), new IPayWithCardTaskCallback() {
                                    @Override
                                    public void onCallback(PaymentResultResponse response) {
                                        onPayWithCardCallback(activityContext, response, callback, true);
                                    }
                                });
                            }

                        }
                    });

                    AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
                    alertDialog = builder
                            .setView(webView)
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    alertDialog.dismiss();

                                    onPaymentComplete(response);

                                    callback.onCallback(response);


                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {

                                }
                            })
                            .show();
                }
            }
            else {

                onPaymentComplete(response);

                callback.onCallback(response);


            }
        }
        else {

            onPaymentComplete(response);
            callback.onCallback(response);


        }
    }

    void onPaymentComplete(PaymentResultResponse paymentResult){

        if (paymentResultListener != null && isCallbackRecieved == false) {
            if (paymentResult != null && paymentResult.getStatus() != ResponseCode.ERROR) {

                if (paymentResult.getPaymentStatus() == null || paymentResult.getPaymentStatus().contains("incomplete")) {

                    paymentResult.setPaymentStatus(ResponseCode.CANCELED.toString());
                    paymentResult.setError(ResponseCode.CANCELED.toString());
                }
            }
            paymentResultListener.onPaymentResult(paymentResult);
            paymentResultListener = null;
        }
    }

    private void getPaymentStatus(String paymentToken, final IPayWithCardTaskCallback callback) {

        RetrievePaymentStatusTask retrievePaymentStatusTask =  new RetrievePaymentStatusTask();

        retrievePaymentStatusTask
                .fillBodyRequest(paymentToken)
                .setCallback(callback)
                .setAuthorizationString("Bearer " + paymentToken)
                .setRequestMethod("GET")
                .execute();
    }

    private void payWithCard(final Activity activityContext, String paymentToken, String cardNumber, String cardCvv, String cardHolder, String cardExpMonth, String cardExpYear, final IPayWithCardTaskCallback callback) {

        PayWithCardTask payWithCardTask =  new PayWithCardTask();

        payWithCardTask
                .fillBodyRequest(paymentToken, cardNumber, cardCvv, cardHolder, cardExpMonth, cardExpYear)
                .setCallback(new IPayWithCardTaskCallback() {
                    @Override
                    public void onCallback(PaymentResultResponse response) {
                        onPayWithCardCallback(activityContext, response, callback, false);
                    }
                })
                .setEndpoint(paymentSettings.getEndpoint() + "payments")
                .setAuthorizationString("Bearer " + paymentToken)
                .execute();
    }

    private void payWithCardJson(final Activity activityContext, String paymentToken, JSONObject creditCardJson, final IPayWithCardTaskCallback callback) {

        PayWithCardTask payWithCardTask =  new PayWithCardTask();

        payWithCardTask
                .setCallback(new IPayWithCardTaskCallback() {
                    @Override
                    public void onCallback(PaymentResultResponse response) {
                        onPayWithCardCallback(activityContext, response, callback, false);
                    }
                })
                .setJsonBody(creditCardJson.toString())
                .setEndpoint(paymentSettings.getEndpoint() + "payments")
                .setAuthorizationString("Bearer " + paymentToken)
                .execute();
    }

    public void getPaymentToken(final IRetrievePaymentTokenTask callback) {

        RetrievePaymentTokenTask paymentTokenTask =  new RetrievePaymentTokenTask();

        try {

            String amount = orderData.getString("amount");
            String currency = orderData.getString("currency");
            String description = orderData.getString("description");

            paymentTokenTask
                    .fillBodyRequest(getPaymentSettings().isTestMode(), "1", amount, currency, description)
                    .setCallback(new IRetrievePaymentTokenTask() {
                        @Override
                        public void onCallback(PaymentTokenResponse response) {

                            paymentTokenResponse = response;

                            callback.onCallback(response);
                        }
                    })
                    .setEndpoint(paymentSettings.getEndpoint() + "checkouts")
                    .setAuthorizationString("Bearer " + publicStoreKey)
                    .execute();

        } catch (JSONException e) {
            PaymentTokenResponse paymentTokenResponse = new PaymentTokenResponse();
            paymentTokenResponse.setError("Invalid order data");
            callback.onCallback(paymentTokenResponse);
        }


    }


}
