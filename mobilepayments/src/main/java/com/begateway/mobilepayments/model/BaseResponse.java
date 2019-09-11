package com.begateway.mobilepayments.model;

import org.json.JSONObject;

public class BaseResponse {

    private String error;

    private String raw;

    private int responseCode;

    private JSONObject rawJson;

    public String getRaw(){
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    /**
    * Get error during request. If no errors return NULL
     */
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * Get response code
     */
    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Get raw json from payment response
     */
    public JSONObject getRawJson() {
        return rawJson;
    }

    public void setRawJson(JSONObject rawJson) {
        this.rawJson = rawJson;
    }

    public ResponseCode getStatus(){

        if (error != null)
        {
            if (getResponseCode() == 408){
                return ResponseCode.CONNECTION_ERROR;
            }
            return ResponseCode.ERROR;
        }

        if (raw != null) return ResponseCode.SUCCESS;

        return ResponseCode.CANCELED;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "error='" + error + '\'' +
                ", responseCode=" + responseCode +
                '}';
    }
}
