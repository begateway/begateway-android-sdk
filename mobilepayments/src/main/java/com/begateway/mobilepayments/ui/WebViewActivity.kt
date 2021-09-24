package com.begateway.mobilepayments.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.begateway.mobilepayments.databinding.BegatewayWebViewActivityBinding
import com.begateway.mobilepayments.sdk.PaymentSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class WebViewActivity : AbstractActivity() {
    companion object {
        private const val THREE_DS_URL = "om.begateway.mobilepayments.THREE_DS_URL"
        fun getThreeDSIntent(context: Activity, url: String) =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(THREE_DS_URL, url)
            }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BegatewayWebViewActivityBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setToolbar(toolbar)
            webView.settings.javaScriptEnabled = true
            webView.settings.allowFileAccess = true
            webView.webChromeClient = object : WebChromeClient() {}
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    return false
                }

                override fun onPageFinished(view: WebView, url: String) {
                    if (url.contains(PaymentSdk.instance.settings.returnUrl, true)) {
                        CoroutineScope(Dispatchers.IO).launch {
                            PaymentSdk.instance.onThreeDSecureComplete()
                        }
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
            webView.loadUrl(intent.getStringExtra(THREE_DS_URL)!!)
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}