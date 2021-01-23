package com.begateway.example

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.begateway.example.databinding.ActivityMainBinding
import com.begateway.mobilepayments.PaymentSdk
import com.begateway.mobilepayments.PaymentSdkBuilder
import com.begateway.mobilepayments.model.*
import com.begateway.mobilepayments.model.network.request.PaymentRequest
import com.begateway.mobilepayments.model.network.request.TokenCheckoutData
import com.begateway.mobilepayments.model.network.response.CheckoutWithTokenData
import com.begateway.mobilepayments.network.HttpResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val REQUEST_PAY = 1

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var sdk: PaymentSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sm3d.isChecked = false
        sdk = PaymentSdkBuilder()
            .setDebugMode()
            .setPublicKey(if (binding.sm3d.isChecked) TestData.PUBLIC_STORE_KEY_3D else TestData.PUBLIC_STORE_KEY)
            .setEndpoint(TestData.YOUR_CHECKOUT_ENDPOINT)
            .build()

        initView()
        listeners()
    }

    private fun initView() {

    }

    private fun listeners() {
        binding.bGetToken.setOnClickListener {
            getPaymentToken()
        }
        binding.bPayWithCreditCard.setOnClickListener {
            payWithCard()
        }
        binding.bPayWithCheckout.setOnClickListener {
            payWithCheckout()
        }
    }

    private fun isProgressVisible(isVisible: Boolean) {
        binding.flProgressBar.isVisible = isVisible
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    // use if you already have payment token
    private fun payWithCheckout() {
        sdk.checkoutWithTokenData = CheckoutWithTokenData(
            CheckoutWithToken(
                token = "572de12513b0aa5f23eb0039dfc77ca3f949cb43efc493b5f9dcb519477004d0"
            )
        )
        startActivityForResult(
            PaymentSdk.getCardFormIntent(this@MainActivity),
            REQUEST_PAY
        )
    }

    // use if you already have payment token and card info(token or card data)
    private fun payWithCard() {
        val token = binding.tilToken.editText?.text?.toString() ?: return
        val cardToken =
            if (binding.sm3d.isChecked) "e94c2a77-5498-45d3-a5b1-3155d0f0bcb3" else "09fde0dc-aec7-4715-8257-b049628596d7"
        GlobalScope.launch(Dispatchers.Main) {
            isProgressVisible(true)
            val result = sdk.payWithCard(
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
            isProgressVisible(false)
            when (result) {
                is HttpResult.Success -> result.data
                is HttpResult.UnSuccess -> result.bepaidResponse
                is HttpResult.Error -> {

                }
            }
        }
    }

    //use if you haven't anything
    private fun getPaymentToken() {
        GlobalScope.launch(Dispatchers.Main) {
            isProgressVisible(true)
            val result = sdk.getPaymentToken(
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
                            autoReturn = 0,
                        ),
                    )
                )
            )
            isProgressVisible(false)
            when (result) {
                is HttpResult.Success -> result.data.let {
                    binding.tilToken.editText?.setText(it.checkout.token)
                    startActivityForResult(
                        PaymentSdk.getCardFormIntent(this@MainActivity),
                        REQUEST_PAY
                    )
                }
                is HttpResult.UnSuccess -> {
                    result.bepaidResponse
                }
                is HttpResult.Error -> {
                    result.exception
                }
            }
        }

    }
}