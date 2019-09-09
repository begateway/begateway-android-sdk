package com.begateway.mobilepayments.model;

import android.util.Log;

import com.begateway.mobilepayments.utils.CardType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PaymentTokenResponse extends BaseResponse {

    private String token;

    private String redirectUrl;

    private List<CardType> supportedCardTypes;

    public List<CardType> getSupportedCardTypes() {
        return supportedCardTypes;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void Fill(JSONObject jsonData) throws JSONException {

        JSONObject checkoutJsonData = jsonData.getJSONObject("checkout");
        setToken(checkoutJsonData.getString("token"));
        setRedirectUrl(checkoutJsonData.getString("redirect_url"));

        // get allowed card types
        supportedCardTypes = new ArrayList<CardType>();
        if (checkoutJsonData.isNull("brands") == false) {
            JSONArray jsonArray = checkoutJsonData.getJSONArray("brands");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String cardTypeName = jsonObject.getString("name").toUpperCase();

                try {
                    CardType cardType = CardType.valueOf(cardTypeName);

                    supportedCardTypes.add(cardType);

                    setRaw(jsonObject.toString());
                    setRawJson(jsonData);

                } catch (IllegalArgumentException e){
                    Log.e("parse token data --> ", cardTypeName + " unsupported ");
                }
            }
        }
    }

}
