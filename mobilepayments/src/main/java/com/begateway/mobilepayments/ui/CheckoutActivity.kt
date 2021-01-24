package com.begateway.mobilepayments.ui

import android.app.Activity
import android.os.Bundle
import com.begateway.mobilepayments.OnResultListener
import com.begateway.mobilepayments.PaymentSdk
import com.begateway.mobilepayments.model.network.response.BepaidResponse

private val TAG_CARD_FORM_SHEET = CardFormBottomDialog::class.java.name

internal class CheckoutActivity : AbstractActivity(), OnResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!PaymentSdk.instance.isSdkInitialized) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        attachCardDialogFragment()
        PaymentSdk.instance.addCallBackListener(this)
    }

    private fun attachCardDialogFragment() {
        findDialog()
            ?: CardFormBottomDialog().show(supportFragmentManager, TAG_CARD_FORM_SHEET)
    }

    override fun onDestroy() {
        detachCardDialogFragment()
        PaymentSdk.instance.removeResultListener(this)
        super.onDestroy()
    }

    private fun detachCardDialogFragment() {
        findDialog()?.dialog?.apply {
            setOnCancelListener(null)
            setOnDismissListener(null)
        }
    }

    private fun findDialog() =
        (supportFragmentManager.findFragmentByTag(TAG_CARD_FORM_SHEET) as CardFormBottomDialog?)

    override fun onTokenReady(token: String) {

    }

    override fun onPaymentFinished(bepaidResponse: BepaidResponse, cardToken: String?) {
        onHideProgress()
        finish()
    }
}