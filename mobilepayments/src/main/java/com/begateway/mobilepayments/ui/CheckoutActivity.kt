package com.begateway.mobilepayments.ui

import android.os.Bundle

private val TAG_CARD_FORM_SHEET = CardFormBottomDialog::class.java.name

internal class CheckoutActivity : AbstractActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CardFormBottomDialog().show(supportFragmentManager, TAG_CARD_FORM_SHEET)
    }
}