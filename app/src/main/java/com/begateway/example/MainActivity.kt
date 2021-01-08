package com.begateway.example

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.begateway.example.databinding.ActivityMainBinding
import com.begateway.mobilepayments.PaymentSdk
import com.begateway.mobilepayments.PaymentSdkBuilder
import com.begateway.mobilepayments.model.TransactionType
import com.begateway.mobilepayments.model.network.request.*
import com.begateway.mobilepayments.network.HttpResult
import com.begateway.mobilepayments.ui.CheckoutActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var sdk: PaymentSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sdk = PaymentSdkBuilder()
//                .setTestMode(true)
            .setDebugMode(true)
            .setEndpoint("https://checkout.begateway.com/ctp/api/")
//                .setPublicKey(TestData.PUBLIC_KEY_3D_ON)
            .build()

        initView()
        listeners()
        startActivity(Intent(this, CheckoutActivity::class.java))
    }

    private fun initView() {

    }

    private fun listeners() {
        binding.bGetToken.setOnClickListener {
            getPaymentToken()
        }
    }

    private fun isProgressVisible(isVisible: Boolean) {
        binding.flProgressBar.isVisible = isVisible
    }

    private fun getPaymentToken() {

        GlobalScope.launch(Dispatchers.Main) {
            isProgressVisible(true)
            val result = sdk.getPaymentToken(
                if (binding.sm3d.isChecked) TestData.PUBLIC_KEY_3D_ON else TestData.PUBLIC_KEY_3D_OFF,
                GetPaymentTokenRequest(
                    Checkout(
                        test = true,
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
                        )
                    )
                )
            )
            isProgressVisible(false)
            when (result) {
                is HttpResult.Success -> result.data
            }
        }

    }
}