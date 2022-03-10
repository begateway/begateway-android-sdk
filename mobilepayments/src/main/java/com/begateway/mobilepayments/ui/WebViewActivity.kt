package com.begateway.mobilepayments.ui

import android.annotation.SuppressLint
import android.content.Context
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
        private const val RESULT_URl = "om.begateway.mobilepayments.RESULT_URl"
        fun getThreeDSIntent(context: Context, url: String, resultUrl: String) =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(THREE_DS_URL, url)
                putExtra(RESULT_URl, resultUrl)
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
                    val resultUrl = intent.getStringExtra(RESULT_URl)!!
                    if (url.contains(resultUrl, true)) {
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