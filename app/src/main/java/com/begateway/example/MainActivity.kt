package com.begateway.example

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isVisible
import com.begateway.example.databinding.ActivityMainBinding
import com.begateway.mobilepayments.models.network.request.*
import com.begateway.mobilepayments.models.network.response.BeGatewayResponse
import com.begateway.mobilepayments.models.network.response.CheckoutWithTokenData
import com.begateway.mobilepayments.sdk.OnResultListener
import com.begateway.mobilepayments.sdk.PaymentSdk
import com.begateway.mobilepayments.sdk.PaymentSdkBuilder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity(), OnResultListener {
    private lateinit var binding: ActivityMainBinding

    private var isWithCheckout: Boolean = false
    private lateinit var sdk: PaymentSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listeners()
    }

    private fun initPaymentSdk() = PaymentSdkBuilder().apply {
        setDebugMode(BuildConfig.DEBUG)
        with(binding) {
            setPublicKey(if (mcb3d.isChecked) TestData.PUBLIC_STORE_KEY_3D else TestData.PUBLIC_STORE_KEY)
            setCardNumberFieldVisibility(mcbCardNumberVisibility.isChecked)
            setCardCVCFieldVisibility(mcbCvvVisibility.isChecked)
            setCardDateFieldVisibility(mcbDateVisibility.isChecked)
            setCardHolderFieldVisibility(mcbHolderVisibility.isChecked)
            setSaveCardVisibility(mcbSaveCardVisibility.isChecked)
        }
        setEndpoint(TestData.YOUR_CHECKOUT_ENDPOINT)
        setReturnUrl("https://DEFAULT_RETURN_URL.com")
    }.build().apply {
        addCallBackListener(this@MainActivity)
    }.also {
        sdk = it
    }

    private fun listeners() {
        binding.bGetToken.setOnClickListener {
            pay()
        }
        binding.bPayWithCreditCard.setOnClickListener {
            if (!isTokenEmpty()) {
                val cardToken = getPreferences().getString("be_paid_card_token", null)
                if (cardToken.isNullOrEmpty()) {
                    getMessageDialog(
                        this,
                        "Error",
                        "There is no card token data",
                        positiveOnClick = { dialog, _ ->
                            dialog.dismiss()
                        },
                        isCancellableOutside = false
                    ).show()
                } else {
                    payWithCard(cardToken)
                }
            }
        }
        binding.bPayWithCheckout.setOnClickListener {
            if (!isTokenEmpty()) {
                isWithCheckout = true
                pay()
            }
        }
    }

    private fun isTokenEmpty(): Boolean =
        (binding.tilToken.editText?.text?.toString().isNullOrEmpty()).also {
            if (it) {
                getMessageDialog(
                    this,
                    "Error",
                    "Please use get token first",
                    positiveOnClick = { dialog, _ ->
                        dialog.dismiss()
                    },
                    isCancellableOutside = false
                ).show()
            }
        }

    private fun isProgressVisible(isVisible: Boolean) {
        binding.flProgressBar.isVisible = isVisible
    }

    /**
     * use if you already have payment token
     */
    private fun payWithCheckout() {
        sdk.checkoutWithTokenData = CheckoutWithTokenData(
            CheckoutWithToken(
                token = binding.tilToken.editText!!.text.toString()
            )
        )
        startActivity(
            PaymentSdk.getCardFormIntent(this@MainActivity)
        )
    }


    /**
     * use if you already have payment token or card info(card token or card data)
     */
    private fun payWithCard(cardToken: String) {
        val token = binding.tilToken.editText?.text?.toString() ?: return
        isProgressVisible(true)
        CoroutineScope(Dispatchers.IO).launch {
            initPaymentSdk().payWithCard(
                PaymentRequest(
                    Request(
                        token,
                        PaymentMethodType.CREDIT_CARD,
                        CreditCard(
                            token = cardToken
                        )
                    )
                ),
                this@MainActivity
            )
        }
    }

    /**
     * use if you haven't anything
     */
    private fun pay() {
        isProgressVisible(true)
        CoroutineScope(Dispatchers.IO).launch {
            initPaymentSdk().getPaymentToken(
                TokenCheckoutData(
                    Checkout(
                        test = BuildConfig.DEBUG,// true only if you work in test mode
                        transactionType = TransactionType.PAYMENT,
                        order = Order(
                            amount = 100,
                            currency = "USD",
                            description = "Payment description",
                            trackingId = "merchant_id",
                            additionalData = AdditionalData(
                                contract = arrayOf(
                                    Contract.RECURRING,
                                    Contract.CARD_ON_FILE
                                )
                            )
                        ),
                        settings = Settings(
                            returnUrl = "https://DEFAULT_RETURN_URL.com",
                            autoReturn = 0,
                        ),
                    ),
                ).apply {
                    addCustomField("customField", "custom string")
                }
            )
        }
    }

    override fun onDestroy() {
        sdk.removeResultListener(this)
        super.onDestroy()
    }

    override fun onTokenReady(token: String) {
        binding.tilToken.editText?.setText(token)
        if (isWithCheckout) {
            payWithCheckout()
            isWithCheckout = false
        } else {
            startActivity(
                PaymentSdk.getCardFormIntent(this@MainActivity)
            )
        }
        isProgressVisible(false)
    }

    private fun getPreferences() = getSharedPreferences("BE_PAID_PREFS", Context.MODE_PRIVATE)
    override fun onPaymentFinished(beGatewayResponse: BeGatewayResponse, cardToken: String?) {
        if (!isFinishing) {
            cardToken?.let {
                getPreferences().edit { putString("be_paid_card_token", cardToken) }
            }
            isWithCheckout = false
            getMessageDialog(
                this,
                "Result",
                beGatewayResponse.message + "; card token=" + cardToken,
                positiveOnClick = { dialog, _ ->
                    dialog.dismiss()
                },
                isCancellableOutside = false
            ).show()
            isProgressVisible(false)
        }
    }

    private fun getMessageDialog(
        context: Context,
        title: String? = null,
        message: String? = null,
        positiveButtonText: String = "ok",
        negativeButtonText: String? = null,
        positiveOnClick: DialogInterface.OnClickListener? = null,
        onCancelClick: DialogInterface.OnClickListener? = null,
        isCancellableOutside: Boolean = false
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(
            context
        )
        title?.let {
            builder.setTitle(it)
        }
        message?.let {
            builder.setMessage(it)
        }
        positiveOnClick?.let {
            builder.setPositiveButton(positiveButtonText, it)
        }
        onCancelClick?.let {
            builder.setNegativeButton(negativeButtonText, it)
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(isCancellableOutside)
        return dialog
    }
}