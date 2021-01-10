package com.begateway.mobilepayments.cardscanners

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.model.CardData
import ru.tinkoff.core.nfc.BaseNfcActivity
import ru.tinkoff.core.nfc.ImperfectAlgorithmException
import ru.tinkoff.core.nfc.MalformedDataException

internal class NfcScannerActivity : BaseNfcActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.begateway_nfc_activity)
    }

    override fun onResult(cardNumber: String, expireDate: String) {
        val intent = Intent()
        intent.putExtra(EXTRA_CARD, CardData(cardNumber, expireDate, ""))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onException(exception: Exception) {
        onException()
    }

    override fun onClarifiedException(ex: MalformedDataException) {
        onException()
    }

    override fun onClarifiedException(ex: ImperfectAlgorithmException) {
        onException()
    }

    override fun getNfcDisabledDialogMessage(): String {
        return getString(R.string.begateway_secure_info)
    }

    override fun getNfcDisabledDialogTitle(): String {
        return getString(R.string.begateway_secure_info)
    }

    private fun onException() {
        setResult(RESULT_ERROR)
        finish()
    }

    internal companion object {
        const val EXTRA_CARD = "card_extra"
        const val RESULT_ERROR = 256
    }
}