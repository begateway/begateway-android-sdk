package com.begateway.mobilepayments.ui

import android.app.Activity
import android.os.Bundle
import com.begateway.mobilepayments.PaymentSdk

private val TAG_CARD_FORM_SHEET = CardFormBottomDialog::class.java.name

internal class CheckoutActivity : AbstractActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PaymentSdk.instance.isSdkInitialized) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        attachCardDialogFragment()
    }

    private fun attachCardDialogFragment() {
        findDialog()
            ?: CardFormBottomDialog().show(supportFragmentManager, TAG_CARD_FORM_SHEET)
    }

    override fun onDestroy() {
        detachCardDialogFragment()
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
}