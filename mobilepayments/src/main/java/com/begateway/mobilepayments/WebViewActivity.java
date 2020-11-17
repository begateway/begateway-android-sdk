package com.begateway.mobilepayments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    private static final String START_URL = "startUrl";
    private static final String RETURN_URL = "returnUrl";

    //it needs for case clicked onBackPressed
    private boolean isCorrectFinish = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.begateway_activity_web_view);
        String startUrl = getIntent().getStringExtra(START_URL);
        final String returnUrl = getIntent().getStringExtra(RETURN_URL);

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.loadUrl(startUrl);
        webView.setWebChromeClient(new WebChromeClient() {
        });

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            public void onPageFinished(WebView view, String url) {
                if (url.toLowerCase().contains(returnUrl.toLowerCase())) {
                    PaymentModule.getInstance().onWebViewFinished();
                    isCorrectFinish = true;
                    finish();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        if (!isCorrectFinish) {
            PaymentModule.getInstance().onWebViewCancelled();
        }
        super.onDestroy();
    }

    static Intent getIntent(Context context, String startUrl, String returnUrl) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(START_URL, startUrl);
        intent.putExtra(RETURN_URL, returnUrl);
        return intent;
    }
}
