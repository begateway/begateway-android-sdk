package com.begateway.mobilepayments.model;

public class PaymentResultResponse extends BaseResponse {

    private String paymentStatus;

    private String token;

    private String tokenCard;

    private String url;

    private boolean isSaveCard;

    /**
     * @return User toggle state of save card checkbox
     */
    public boolean isSaveCard() {
        return isSaveCard;
    }

    public void setSaveCard(boolean saveCard) {
        isSaveCard = saveCard;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return Token of card
     */
    public String getTokenCard() {
        return tokenCard;
    }

    public void setTokenCard(String tokenCard) {
        this.tokenCard = tokenCard;
    }

    /**
     * @return Payment token
     */
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return Status of request
     * ResponseCode.SUCCESS, ResponseCode.CANCELED, ResponseCode.ERROR, ResponseCode.TIMEOUT
     */
    @Override
    public ResponseCode getStatus() {

        if (getError() != null && getError().toLowerCase().contains("canceled")){
            return ResponseCode.CANCELED;
        }

        if (getPaymentStatus() != null && getPaymentStatus().toLowerCase().contains("failed")){
            return ResponseCode.FAILED;
        }

        if (getPaymentStatus() != null && getPaymentStatus().toLowerCase().contains("incomplete")){
            return ResponseCode.INCOMPLETE;
        }

        return super.getStatus();
    }

    /**
     * @return Payment status that returned in payment response
     */
    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
