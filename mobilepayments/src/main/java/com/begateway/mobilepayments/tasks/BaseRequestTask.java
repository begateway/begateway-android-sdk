package com.begateway.mobilepayments.tasks;

import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.begateway.mobilepayments.model.BaseResponse;
import com.begateway.mobilepayments.model.ResponseCode;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class BaseRequestTask<T extends BaseResponse> extends AsyncTask<Object, Void, T>  {

    private String requestMethod = "POST";

    private String endpoint = "";

    private String authorizationString = "";

    private String jsonBody = "{}";

    public BaseRequestTask setJsonBody(String jsonBody) {
        this.jsonBody = jsonBody;

        return this;
    }

    public BaseRequestTask setEndpoint(String endpoint) {
        this.endpoint = endpoint;

        return this;
    }

    public BaseRequestTask setAuthorizationString(String authorizationString) {
        this.authorizationString = authorizationString;

        return this;
    }

    public BaseRequestTask setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;

        return this;
    }

    public BaseResponse getResponseInstance(){
        return new BaseResponse();
    }

    protected void log(String message){

        Log.w("RequestTask", message);
    }

    protected T doInBackground(Object... urls) {

        T responseData = (T) getResponseInstance();

        try {

            if (requestMethod.contains("GET")) {
                try {
                    Thread.currentThread();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            URL url = new URL(endpoint);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            log(" --> " + jsonBody);

            if (requestMethod.contains("GET") == false) {
                con.setRequestMethod(requestMethod);
                con.setDoOutput(true);
            }
            con.addRequestProperty("Content-Type", "application/json; utf-8");
            con.addRequestProperty("Accept", "application/json");

            con.addRequestProperty("X-Api-Version", "2");
            con.addRequestProperty("Authorization", authorizationString);
            con.setDoInput(true);

            con.setConnectTimeout(1500);

            if (requestMethod.contains("GET") == false) {
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }


            int responseCode = con.getResponseCode();
            responseData.setResponseCode(responseCode);

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {

                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String responseString = response.toString();
                responseData.setRaw(responseString);

                responseData.setRaw(responseString);

                JSONObject rawJson = new JSONObject(responseString);
                responseData.setRawJson(rawJson);
            }
        } catch (Exception e) {

            responseData.setError(e.toString());
        }

        if (responseData.getStatus() == ResponseCode.ERROR)
        {
            log(" <-- " + responseData.getError());
        }
        else if (responseData.getStatus() != ResponseCode.SUCCESS)
        {
            log(" <-- " + responseData.getStatus().toString());
        }
        else {
            log(" <-- " + responseData.getRawJson().toString());
        }


        return responseData;
    }

    protected void onPostExecute(BaseResponse response) {

    }

}