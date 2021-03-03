package com.begateway.mobilepayments.cardScanner.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.begateway.mobilepayments.databinding.BegatewayNfcScannerActivityBinding
import com.begateway.mobilepayments.models.ui.CardData
import com.begateway.mobilepayments.ui.AbstractActivity
import ru.tinkoff.core.nfc.ImperfectAlgorithmException
import ru.tinkoff.core.nfc.MalformedDataException
import ru.tinkoff.core.nfc.NfcAutoRecognizer
import ru.tinkoff.core.nfc.NfcRecognizer.NfcCallbacks
import ru.tinkoff.core.nfc.NfcRecognizer.NfcClarifyCallbacks
import ru.tinkoff.core.nfc.NfcUtils

private const val CARD_NUMBER_KEY = "card_number"
private const val EXPIRY_DATE_KEY = "expire_date"


internal class NfcScannerActivity : AbstractActivity(), NfcCallbacks, NfcClarifyCallbacks {
    companion object {
        fun getIntent(activity: Activity) = Intent(activity, NfcScannerActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BegatewayNfcScannerActivityBinding.inflate(layoutInflater).apply {
            setContentView(root)
            mbClose.setOnClickListener {
                onBackPressed()
            }
            setToolBar(
                toolbar,
            )
        }
        NfcAutoRecognizer(this, this).registerClarifyCallbacks(this)
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

    private fun onException() {
        showMessageDialog(
            this,
            title = "Ошибка",
            message = "В процессе сканирования произошла ошибка",
            positiveButtonText = "Ок",
            positiveOnClick = { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            }
        )
    }

    override fun onNfcDisabled() {
        showMessageDialog(
            this,
            "Ошибка",
            "Включите NFC",
            "Включить",
            "Отмена",
            { dialog, _ ->
                dialog.dismiss()
                NfcUtils.openNfcSettingsForResult(this@NfcScannerActivity, 0)
            },
            { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            }
        )
    }

    override fun onNfcNotSupported() {
        showMessageDialog(
            this,
            title = "Ошибка",
            message = "NFC не поддерживается",
            positiveButtonText = "Ок",
            positiveOnClick = { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            }
        )
    }

    override fun onResult(bundle: Bundle?) {
        setResult(
            Activity.RESULT_OK,
            CardData.getIntentWithExpiryString(
                cardNumber = bundle?.getString(CARD_NUMBER_KEY),
                expiryString = bundle?.getString(EXPIRY_DATE_KEY)
            )
        )
        finish()
    }
}