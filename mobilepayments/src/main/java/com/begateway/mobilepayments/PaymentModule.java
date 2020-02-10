package com.begateway.mobilepayments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.begateway.mobilepayments.utils.RSA;

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

    }

    String encrypt(String dataString, String publicKey) {
        return RSA.encryptData(dataString, publicKey);
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
     *          "number":"*",
     *          "toke":"12345678"
     *       }
     *    }
     * }
     */
    public void payWithCreditCard(JSONObject creditCardJsonData) {

        prepareForPayment();

        if (checkConnection() == false) return;

        if (creditCardJsonData == null){
            PaymentResultResponse paymentResultResponse = new PaymentResultResponse();
            paymentResultResponse.setError("Invalid json");
            onPaymentComplete(paymentResultResponse);
            return;
        }

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

        if (checkConnection() == false) return;

        if (orderData == null){
            PaymentResultResponse paymentResultResponse = new PaymentResultResponse();
            paymentResultResponse.setError("Invalid json");
            onPaymentComplete(paymentResultResponse);
            return;
        }

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

        if (checkConnection() == false) return;

        if (checkoutJsonData == null){
            PaymentResultResponse paymentResultResponse = new PaymentResultResponse();
            paymentResultResponse.setError("Invalid json");
            onPaymentComplete(paymentResultResponse);
            return;
        }

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

    private boolean checkConnection(){

        if (isConnected() == false){
            PaymentResultResponse result = new PaymentResultResponse();
            result.setError("Check your connection");
            result.setResponseCode(408);
            onPaymentComplete(result);

            return false;
        }

        return true;
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
    public String encryptCardData(String cardData, String publicKey){
        try {

            if (cardData.isEmpty()){
                return cardData;
            }
            String encryptedData = encrypt(cardData, publicKey);

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

        if (response.getStatus() == ResponseCode.SUCCESS || response.getStatus() == ResponseCode.INCOMPLETE) {

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

                            if (url.toLowerCase().contains(paymentSettings.getReturnUrl().toLowerCase())) {

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

    public boolean isConnected(){

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    void onPaymentComplete(PaymentResultResponse paymentResult){

        if (paymentResultListener != null && isCallbackRecieved == false) {
//            if (paymentResult != null && paymentResult.getStatus() != ResponseCode.ERROR) {
//
//                if (paymentResult.getPaymentStatus() == null) {
//
//                    paymentResult.setPaymentStatus(ResponseCode.CANCELED.toString());
//                    paymentResult.setError(ResponseCode.CANCELED.toString());
//                }
//                else if (paymentResult.getPaymentStatus().contains("incomplete")){
//                    paymentResult.setPaymentStatus(ResponseCode.INCOMPLETE.toString());
//                }
//            }
            paymentResultListener.onPaymentResult(paymentResult);
            isCallbackRecieved = true;
        }
    }

    private void getPaymentStatus(String paymentToken, final IPayWithCardTaskCallback callback) {

        RetrievePaymentStatusTask retrievePaymentStatusTask =  new RetrievePaymentStatusTask();

        retrievePaymentStatusTask
                .fillBodyRequest(paymentToken)
                .setCallback(callback)
                .setAuthorizationString("Bearer " + paymentToken)
                .setRequestMethod("GET")
                .setDebugMode(getPaymentSettings().isDebugMode())
                .execute();
    }

    private void payWithCard(final Activity activityContext, String paymentToken, String cardNumber, String cardCvv, String cardHolder, String cardExpMonth, String cardExpYear, final IPayWithCardTaskCallback callback) {

        PayWithCardTask payWithCardTask =  new PayWithCardTask();

        boolean isUseEnctyptedCard = paymentSettings.isUseEnctyptedCard();

        String targetPublicKey = (publicStoreKey != null && publicStoreKey.isEmpty()) ? paymentSettings.getPublicKey() : publicStoreKey;

        String targetCardNumber = isUseEnctyptedCard ? encryptCardData(cardNumber, targetPublicKey) : cardNumber;
        String targetCardCvv = isUseEnctyptedCard ? encryptCardData(cardCvv, targetPublicKey) : cardCvv;
        String targetCardHolder = isUseEnctyptedCard ? encryptCardData(cardHolder, targetPublicKey) : cardHolder;
        String targetCardExpMonth = isUseEnctyptedCard ? encryptCardData(cardExpMonth, targetPublicKey) : cardExpMonth;
        String targetCardExpYear = isUseEnctyptedCard ? encryptCardData(cardExpYear, targetPublicKey) : cardExpYear;

        payWithCardTask
                .fillBodyRequest(paymentToken, targetCardNumber, targetCardCvv, targetCardHolder, targetCardExpMonth, targetCardExpYear, isUseEnctyptedCard)
                .setCallback(new IPayWithCardTaskCallback() {
                    @Override
                    public void onCallback(PaymentResultResponse response) {
                        onPayWithCardCallback(activityContext, response, callback, false);
                    }
                })
                .setEndpoint(paymentSettings.getEndpoint() + "payments")
                .setAuthorizationString("Bearer " + paymentToken)
                .setDebugMode(getPaymentSettings().isDebugMode())
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
                .setDebugMode(getPaymentSettings().isDebugMode())
                .execute();
    }

    public void getPaymentToken(String publicStoreKey, JSONObject orderData, final IRetrievePaymentTokenTask callback) {

        if (isConnected() == false){
            PaymentTokenResponse result = new PaymentTokenResponse();
            result.setError("Check your connection");
            result.setResponseCode(408);

            paymentTokenResponse = result;
            callback.onCallback(result);
            return;
        }

        RetrievePaymentTokenTask paymentTokenTask =  new RetrievePaymentTokenTask();

        try {

            String amount = orderData.getString("amount");
            String currency = orderData.getString("currency");
            String description = orderData.getString("description");

            paymentTokenTask
                    .fillBodyRequest(getPaymentSettings().isTestMode(), getPaymentSettings().getAttempts(), amount, currency, description, getPaymentSettings().getNotificationUrl(), getPaymentSettings().getReturnUrl(), getPaymentSettings().getTransactionType())
                    .setCallback(new IRetrievePaymentTokenTask() {
                        @Override
                        public void onCallback(PaymentTokenResponse response) {

                            paymentTokenResponse = response;

                            callback.onCallback(response);
                        }
                    })
                    .setEndpoint(paymentSettings.getEndpoint() + "checkouts")
                    .setAuthorizationString("Bearer " + publicStoreKey)
                    .setDebugMode(getPaymentSettings().isDebugMode())
                    .execute();

        } catch (JSONException e) {
            PaymentTokenResponse paymentTokenResponse = new PaymentTokenResponse();
            paymentTokenResponse.setError("Invalid order data");
            callback.onCallback(paymentTokenResponse);
        }
    }

    public void getPaymentToken(final IRetrievePaymentTokenTask callback) {

        if (isConnected() == false){
            PaymentTokenResponse result = new PaymentTokenResponse();
            result.setError("Check your connection");
            result.setResponseCode(408);

            paymentTokenResponse = result;
            callback.onCallback(result);
            return;
        }

        RetrievePaymentTokenTask paymentTokenTask =  new RetrievePaymentTokenTask();

        try {

            String amount = orderData.getString("amount");
            String currency = orderData.getString("currency");
            String description = orderData.getString("description");

            paymentTokenTask
                    .fillBodyRequest(getPaymentSettings().isTestMode(), getPaymentSettings().getAttempts(), amount, currency, description, getPaymentSettings().getNotificationUrl(), getPaymentSettings().getReturnUrl(), getPaymentSettings().getTransactionType())
                    .setCallback(new IRetrievePaymentTokenTask() {
                        @Override
                        public void onCallback(PaymentTokenResponse response) {

                            paymentTokenResponse = response;

                            callback.onCallback(response);
                        }
                    })
                    .setEndpoint(paymentSettings.getEndpoint() + "checkouts")
                    .setAuthorizationString("Bearer " + publicStoreKey)
                    .setDebugMode(getPaymentSettings().isDebugMode())
                    .execute();

        } catch (JSONException e) {
            PaymentTokenResponse paymentTokenResponse = new PaymentTokenResponse();
            paymentTokenResponse.setError("Invalid order data");
            callback.onCallback(paymentTokenResponse);
        }
    }


}
