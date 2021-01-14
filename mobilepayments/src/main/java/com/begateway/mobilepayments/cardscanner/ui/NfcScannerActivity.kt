package com.begateway.mobilepayments.cardscanner.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.databinding.BegatewayNfcScannerActivityBinding
import com.begateway.mobilepayments.model.CardData
import com.begateway.mobilepayments.ui.AbstractActivity
import ru.tinkoff.core.nfc.ImperfectAlgorithmException
import ru.tinkoff.core.nfc.MalformedDataException
import ru.tinkoff.core.nfc.NfcAutoRecognizer
import ru.tinkoff.core.nfc.NfcRecognizer.NfcCallbacks
import ru.tinkoff.core.nfc.NfcRecognizer.NfcClarifyCallbacks
import ru.tinkoff.core.nfc.NfcUtils

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
                ContextCompat.getColor(this@NfcScannerActivity, R.color.begateway_primary_black),
                ContextCompat.getColor(this@NfcScannerActivity, R.color.begateway_color_accent)
            ) {
                onBackPressed()
            }
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
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage("В процессе сканирования произошла ошибка")
            .setPositiveButton("Ок") { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            }
            .show()
    }

    override fun onNfcDisabled() {
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage("Включите NFC")
            .setPositiveButton("Включить") { dialog, _ ->
                dialog.dismiss()
                NfcUtils.openNfcSettingsForResult(this@NfcScannerActivity, 0)
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            }
            .show()
    }

    override fun onNfcNotSupported() {
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage("NFC не поддерживается")
            .setPositiveButton("Ок") { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            }
            .show()
    }

    override fun onResult(bundle: Bundle?) {
        setResult(
            Activity.RESULT_OK,
            CardData.getIntentWithExpiryString(
                cardNumber = bundle?.getString("card_number"),
                expiryString = bundle?.getString("expire_date")
            )
        )
        finish()
    }
}