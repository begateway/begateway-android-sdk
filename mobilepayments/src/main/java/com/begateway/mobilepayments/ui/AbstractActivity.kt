package com.begateway.mobilepayments.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.begateway.mobilepayments.BuildConfig
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.utils.applyTintColorOnDrawable
import com.begateway.mobilepayments.utils.getProgressDialog

private const val IS_DIALOG_SHOWED = "com.begateway.mobilepayments.IS_PROGRESS_SHOWED"

internal abstract class AbstractActivity : AppCompatActivity(), OnProgressDialogListener {

    private var isDialogWasShowed: Boolean = false
    private var progressDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!BuildConfig.DEBUG)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            isDialogWasShowed = it.getBoolean(IS_DIALOG_SHOWED)
        }
    }

    override fun onStart() {
        super.onStart()
        if (isDialogWasShowed) {
            onShowProgress()
        }
    }

    fun setToolBar(
        toolbar: Toolbar,
        @ColorInt navIconColor: Int = ContextCompat.getColor(
            this@AbstractActivity,
            R.color.begateway_primary_black
        ),
        @ColorInt toolbarBackgroundColor: Int = ContextCompat.getColor(
            this@AbstractActivity,
            R.color.begateway_color_accent
        ),
        title: String? = null,
        onBackPressedAction: (() -> Unit)? = { onBackPressed() }
    ) {
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            toolbar.navigationIcon?.applyTintColorOnDrawable(navIconColor)
            toolbar.setNavigationOnClickListener {
                onBackPressedAction?.invoke() ?: onBackPressed()
            }
            toolbar.setBackgroundColor(toolbarBackgroundColor)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(IS_DIALOG_SHOWED, isDialogWasShowed)
        super.onSaveInstanceState(outState)
    }

    override fun onShowProgress() {
        onHideProgress()
        progressDialog = getProgressDialog(
            this,
            R.layout.begateway_progress,
            layoutInflater
        )
        progressDialog?.show()
        isDialogWasShowed = true
    }

    override fun onDestroy() {
        onHideProgress()
        super.onDestroy()
    }

    override fun onHideProgress() {
        progressDialog?.dismiss()
        isDialogWasShowed = false
    }
}