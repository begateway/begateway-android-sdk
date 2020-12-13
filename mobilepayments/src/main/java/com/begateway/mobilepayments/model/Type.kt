package com.begateway.mobilepayments.model

import com.google.gson.annotations.SerializedName

enum class Type {
    @SerializedName("delayed_charge") DELAYED_CHARGE,
    @SerializedName("increment") INCREMENT,
    @SerializedName("resubmission") RESUBMISSION,
    @SerializedName("reauthorization") REAUTHORIZATION,
    @SerializedName("no_show") NO_SHOW,
}