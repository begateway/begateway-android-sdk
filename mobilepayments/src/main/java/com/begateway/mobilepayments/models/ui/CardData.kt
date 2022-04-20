package com.begateway.mobilepayments.models.ui

import android.content.Intent
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
internal data class CardData(
    val cardNumber: String? = null,
    val cardHolderName: String? = null,
    val expiryDate: Date? = null,
    val cvcCode: String? = null
) : Parcelable {
    companion object {
        private const val EXPIRY_DATE_FORMAT_FULL = "MM/yyyy"
        const val EXPIRY_DATE_FORMAT_SMALL = "MM/yy"
        private const val EXPIRY_MONTH = "MM"
        private const val EXPIRY_YEAR = "yy"
        private const val EXTRA_CARD = "card_extra"

        fun getExpiryDateStringForView(date: Date?) = date?.let {
            SimpleDateFormat(EXPIRY_DATE_FORMAT_SMALL, Locale.US).format(it)
        }

        fun getExpiryDateFromString(string: String?): Date? {
            string ?: return null
            return try {
                SimpleDateFormat(
                    when (string.length) {
                        EXPIRY_DATE_FORMAT_SMALL.length -> EXPIRY_DATE_FORMAT_SMALL
                        else -> EXPIRY_DATE_FORMAT_FULL
                    },
                    Locale.US
                ).parse(string)
            } catch (e: ParseException) {
                null
            }
        }

        fun getIntent(
            cardNumber: String? = null,
            cardHolderName: String? = null,
            expiryMonth: String? = null,
            expiryYear: String? = null,
            cvcCode: String? = null
        ) = getIntentWithExpiryString(
            cardNumber,
            cardHolderName,
            if (expiryMonth != null && expiryYear != null) {
                "$expiryMonth/$expiryYear"
            } else {
                null
            },
            cvcCode
        )

        fun getIntentWithExpiryString(
            cardNumber: String? = null,
            cardHolderName: String? = null,
            expiryString: String? = null,
            cvcCode: String? = null
        ) = Intent().apply {
            putExtra(
                EXTRA_CARD, CardData(
                    cardNumber,
                    cardHolderName,
                    getExpiryDateFromString(expiryString),
                    cvcCode
                )
            )
        }


        fun getDataFromIntent(intent: Intent?): CardData? = intent?.getParcelableExtra(EXTRA_CARD)
    }

    fun getMonth(): String? = expiryDate?.let {
        SimpleDateFormat(EXPIRY_MONTH, Locale.US).format(it)
    }

    fun getYear(): String? = expiryDate?.let {
        SimpleDateFormat(EXPIRY_YEAR, Locale.US).format(it)
    }
}
