package com.begateway.mobilepayments.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class NetworkUtils {

    public static String getAuthToken(String username, String password) {
        byte[] data = new byte[0];
        try {
            data = (username + ":" + password).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }


}
