package com.begateway.mobilepayments.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.models.network.response.BeGatewayResponse
import com.begateway.mobilepayments.sdk.OnResultListener
import com.begateway.mobilepayments.sdk.PaymentSdk
import com.google.android.gms.wallet.AutoResolveHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val TAG_CARD_FORM_SHEET = CardFormBottomDialog::class.java.name
internal const val GOOGLE_PAY_RETURN_CODE = 0x54BD

internal class CheckoutActivity : AbstractActivity(), OnResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!PaymentSdk.instance.isSdkInitialized) {
            PaymentSdk.instance.onPaymentFinished(BeGatewayResponse(message = "SDK not initialized"))
            finish()
            return
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GOOGLE_PAY_RETURN_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    onShowProgress()
                    CoroutineScope(Dispatchers.Main).launch {
                        PaymentSdk.instance.payWithGooglePay(data)
                    }
                } else {
                    if (resultCode == AutoResolveHelper.RESULT_ERROR) {
                        showMessageDialog(
                            this,
                            R.string.begateway_error,
                            R.string.begateway_error,
                            positiveOnClick = { dialog, _ ->
                                dialog.dismiss()
                            }
                        )
                    }
                    onHideProgress()
                }
            }
        }
    }

    override fun onPaymentFinished(beGatewayResponse: BeGatewayResponse, cardToken: String?) {
        onHideProgress()
        finish()
    }
}