package com.begateway.mobilepayments.model.network.request

import com.google.gson.annotations.SerializedName

enum class Contract {
    @SerializedName("recurring") RECURRING,
    @SerializedName("oneclick") ONE_CLICK,
    @SerializedName("credit") CREDIT,
    /** use only when transaction_type=authorization */
    @SerializedName("card_on_file") CARD_ON_FILE,
}