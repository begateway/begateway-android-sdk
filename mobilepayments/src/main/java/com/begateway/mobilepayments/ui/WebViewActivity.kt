package com.begateway.mobilepayments.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.begateway.mobilepayments.PaymentSdk
import com.begateway.mobilepayments.databinding.BegatewayWebViewActivityBinding

internal class WebViewActivity : AbstractActivity() {
    companion object {
        private const val THREE_DS_URL = "om.begateway.mobilepayments.THREE_DS_URL"
        fun getThreeDSIntent(context: Activity, url: String) =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(THREE_DS_URL, url)
            }
    }

    private var isCorrect: Boolean = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BegatewayWebViewActivityBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setToolBar(toolbar)
            webView.settings.javaScriptEnabled = true
            webView.settings.allowFileAccess = true
            webView.webChromeClient = object : WebChromeClient() {}
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    return false
                }

                override fun onPageFinished(view: WebView, url: String) {
                    if (url.contains(PaymentSdk.instance.settings.returnUrl, true)) {
                        isCorrect = true
                        finish()
                    }
                }
            }
            webView.loadUrl(intent.getStringExtra(THREE_DS_URL)!!)
        }
    }

    override fun onDestroy() {
        PaymentSdk.instance.onThreeDSecureFinished(isCorrect)
        super.onDestroy()
    }
}