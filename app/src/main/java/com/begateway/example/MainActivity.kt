package com.begateway.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.begateway.example.databinding.ActivityMainBinding
import com.begateway.mobilepayments.PaymentSdk
import com.begateway.mobilepayments.PaymentSdkBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var sdk: PaymentSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sdk = PaymentSdkBuilder()
                .setTestMode(true)
                .setDebugMode(true)
                .setEndpoint("https://checkout.begateway.com/ctp/api/checkouts/")
                .setPublicKey(TestData.PUBLIC_KEY_3D_ON)
                .build()

        initView()
    }

    private fun initView() {

    }

    private fun listeners() {
        binding.bGetToken.setOnClickListener {

        }
    }

    private fun isProgressVisible(isVisible: Boolean) {
        binding.flProgressBar.isVisible = isVisible
    }
}