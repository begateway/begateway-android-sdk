package com.begateway.mobilepayments.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardData(
    val cardNumber: String,
    val expiryDate: String,
    val cvcCode: String
) : Parcelable
