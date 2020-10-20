package com.begateway.mobilepayments.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.begateway.mobilepayments.R

class WebViewDialog(private val webview: WebView) : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.begateway_dialog_web, container, false)
        webview.isFocusable = true
        webview.isFocusableInTouchMode = true
        (root as FrameLayout).addView(webview)
        return root
    }
    fun setOnCancelListener(listenter : DialogInterface.OnCancelListener) {
        dialog.setOnCancelListener(listenter)
    }

    fun setOnDismissListener(listenter : DialogInterface.OnDismissListener) {
        dialog.setOnDismissListener(listenter)
    }
}