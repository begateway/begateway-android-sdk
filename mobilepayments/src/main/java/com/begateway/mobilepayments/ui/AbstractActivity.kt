package com.begateway.mobilepayments.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.begateway.mobilepayments.BuildConfig
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.utils.applyTintColorOnDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val IS_DIALOG_SHOWED = "com.begateway.mobilepayments.IS_PROGRESS_SHOWED"

internal abstract class AbstractActivity : AppCompatActivity(), OnProgressDialogListener {

    private var isDialogWasShowed: Boolean = false
    private var progressDialog: AlertDialog? = null
    protected var alertDialog: AlertDialog? = null
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
        if (!isFinishing) {
            progressDialog = getProgressDialog(
                this,
                R.layout.begateway_progress,
                layoutInflater
            )
            progressDialog?.show()
            isDialogWasShowed = true
        }
    }

    override fun onDestroy() {
        onHideProgress()
        onDismissAlertDialog()
        super.onDestroy()
    }

    private fun onDismissAlertDialog() {
        alertDialog?.dismiss()
        alertDialog = null
    }

    override fun onHideProgress() {
        progressDialog?.dismiss()
        progressDialog = null
        isDialogWasShowed = false
    }

    private fun getProgressDialog(
        context: Context,
        layout: Int,
        layoutInflater: LayoutInflater,
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(
            context,
            R.style.begateway_ShapeAppearanceOverlay_AlertDialog_Rounded
        )
        val customLayout: View =
            layoutInflater.inflate(layout, null)
        builder.setView(customLayout)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    protected fun showMessageDialog(
        context: Context,
        title: String? = null,
        message: String? = null,
        positiveButtonText: String = "ok",
        negativeButtonText: String = "cancel",
        positiveOnClick: DialogInterface.OnClickListener? = null,
        onCancelClick: DialogInterface.OnClickListener? = null,
        isCancellableOutside: Boolean = false
    ) {
        onDismissAlertDialog()
        if (!isFinishing) {
            alertDialog = getMessageDialog(
                context,
                title,
                message,
                positiveButtonText,
                negativeButtonText,
                positiveOnClick,
                onCancelClick,
                isCancellableOutside
            )
            alertDialog?.show()
        }

    }

    private fun getMessageDialog(
        context: Context,
        title: String? = null,
        message: String? = null,
        positiveButtonText: String,
        negativeButtonText: String,
        positiveOnClick: DialogInterface.OnClickListener? = null,
        onCancelClick: DialogInterface.OnClickListener? = null,
        isCancellableOutside: Boolean = false
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(
            context,
            R.style.begateway_ShapeAppearanceOverlay_AlertDialog_Rounded
        )
        title?.let {
            builder.setTitle(it)
        }
        message?.let {
            builder.setTitle(it)
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