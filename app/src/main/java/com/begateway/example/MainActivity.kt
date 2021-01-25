package com.begateway.example

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.begateway.example.databinding.ActivityMainBinding
import com.begateway.mobilepayments.OnResultListener
import com.begateway.mobilepayments.PaymentSdk
import com.begateway.mobilepayments.PaymentSdkBuilder
import com.begateway.mobilepayments.model.*
import com.begateway.mobilepayments.model.network.request.PaymentRequest
import com.begateway.mobilepayments.model.network.request.TokenCheckoutData
import com.begateway.mobilepayments.model.network.response.BepaidResponse
import com.begateway.mobilepayments.model.network.response.CheckoutWithTokenData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity(), OnResultListener {
    private lateinit var binding: ActivityMainBinding

    private var isWithCheckout: Boolean = false
    private lateinit var sdk: PaymentSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sm3d.isChecked = false
        sdk = PaymentSdkBuilder()
            .setDebugMode(BuildConfig.DEBUG)
            .setPublicKey(if (binding.sm3d.isChecked) TestData.PUBLIC_STORE_KEY_3D else TestData.PUBLIC_STORE_KEY)
            .setEndpoint(TestData.YOUR_CHECKOUT_ENDPOINT)
            .setReturnUrl("https://DEFAULT_RETURN_URL.com")
            .build(this, this, CoroutineScope(Dispatchers.IO))
        initView()
        listeners()
        binding.sm3d.setOnCheckedChangeListener { _, isChecked ->
            sdk.updatePublicKey(if (isChecked) TestData.PUBLIC_STORE_KEY_3D else TestData.PUBLIC_STORE_KEY)
        }
    }

    private fun initView() {

    }

    private fun listeners() {
        binding.bGetToken.setOnClickListener {
            isWithCheckout = false
            pay()
        }
        binding.bPayWithCreditCard.setOnClickListener {
            payWithCard()
        }
        binding.bPayWithCheckout.setOnClickListener {
            isWithCheckout = true
            pay()
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
     * use if you already have payment token and card info(card token or card data)
     */
    private fun payWithCard() {
        val token = binding.tilToken.editText?.text?.toString() ?: return
        val cardToken =
            if (binding.sm3d.isChecked) "e94c2a77-5498-45d3-a5b1-3155d0f0bcb3" else "09fde0dc-aec7-4715-8257-b049628596d7"
        isProgressVisible(true)
        sdk.payWithCard(
            PaymentRequest(
                Request(
                    token,
                    PaymentMethodType.CREDIT_CARD,
                    CreditCard(
                        token = cardToken
                    )
                )
            )
        )
    }

    /**
     * use if you haven't anything
     */
    private fun pay() {
        isProgressVisible(true)
        sdk.getPaymentToken(
            TokenCheckoutData(
                Checkout(
                    test = true,// true only if you work in test mode
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
                )
            )
        )
    }

    override fun onDestroy() {
        sdk.removeResultListener(this)
        sdk.resetActivity()//important for memory leaking
        super.onDestroy()
    }

    override fun onTokenReady(token: String) {
        binding.tilToken.editText?.setText(token)
        if (isWithCheckout) {
            payWithCheckout()
        } else {
            startActivity(
                PaymentSdk.getCardFormIntent(this@MainActivity)
            )
        }
        isProgressVisible(false)
    }

    override fun onPaymentFinished(bepaidResponse: BepaidResponse, cardToken: String?) {
        getMessageDialog(
            this,
            "Result",
            bepaidResponse.message,
            positiveOnClick = { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            },
            isCancellableOutside = false
        ).show()
        isProgressVisible(false)
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