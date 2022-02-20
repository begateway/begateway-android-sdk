package com.begateway.mobilepayments.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.sdk.PaymentSdk
import com.begateway.mobilepayments.ui.intefaces.OnActionbarSetup
import com.begateway.mobilepayments.ui.intefaces.OnMessageDialogListener
import com.begateway.mobilepayments.ui.intefaces.OnProgressDialogListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val IS_PROGRESS_DIALOG_SHOWED = "com.begateway.mobilepayments.IS_PROGRESS_SHOWED"

internal abstract class AbstractActivity : AppCompatActivity(),
    OnProgressDialogListener,
    OnMessageDialogListener,
    OnActionbarSetup {

    private var isDialogWasShowed: Boolean = false
    private var progressDialog: AlertDialog? = null
    protected var alertDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!PaymentSdk.instance.sdkSettings.isDebugMode)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        savedInstanceState?.let {
            isDialogWasShowed = it.getBoolean(IS_PROGRESS_DIALOG_SHOWED)
        }
    }

    override fun onStart() {
        super.onStart()
        if (isDialogWasShowed) {
            onShowProgress()
        }
    }

    protected fun setToolbar(toolbar: Toolbar) = addToolBar(toolbar, null, ::onBackPressed)
    override fun addToolBar(toolbar: Toolbar, title: String?, onBackPressedAction: (() -> Unit)?) {
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            toolbar.setNavigationOnClickListener {
                onBackPressedAction?.invoke() ?: onBackPressed()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(IS_PROGRESS_DIALOG_SHOWED, isDialogWasShowed)
        super.onSaveInstanceState(outState)
    }

    override fun onShowProgress() {
        if (!isFinishing && progressDialog?.isShowing != true) {
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
        val builder = MaterialAlertDialogBuilder(context)
        val customLayout: View =
            layoutInflater.inflate(layout, null)
        builder.setView(customLayout)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun showMessageDialog(
        context: Context,
        titleId: Int?,
        messageId: Int?,
        positiveButtonTextId: Int?,
        negativeButtonTextId: Int?,
        positiveOnClick: DialogInterface.OnClickListener?,
        onCancelClick: DialogInterface.OnClickListener?,
        isCancellableOutside: Boolean
    ): AlertDialog? {
        onDismissAlertDialog()
        return if (!isFinishing) {
            val builder = MaterialAlertDialogBuilder(
                context
            )
            titleId?.let {
                builder.setTitle(it)
            }
            messageId?.let {
                builder.setMessage(it)
            }
            positiveOnClick?.let {
                builder.setPositiveButton(positiveButtonTextId ?: R.string.begateway_ok, it)
            }
            onCancelClick?.let {
                builder.setNegativeButton(negativeButtonTextId ?: R.string.begateway_cancel, it)
            }
            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(isCancellableOutside)
            alertDialog = dialog
            alertDialog?.show()
            dialog
        } else {
            null
        }
    }
}