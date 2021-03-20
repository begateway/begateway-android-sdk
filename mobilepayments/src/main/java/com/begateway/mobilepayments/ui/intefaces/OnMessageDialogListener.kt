package com.begateway.mobilepayments.ui.intefaces

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

interface OnMessageDialogListener {
    fun showMessageDialog(
        context: Context,
        @StringRes titleId: Int? = null,
        @StringRes messageId: Int? = null,
        @StringRes positiveButtonTextId: Int? = null,
        @StringRes negativeButtonTextId: Int? = null,
        positiveOnClick: DialogInterface.OnClickListener? = null,
        onCancelClick: DialogInterface.OnClickListener? = null,
        isCancellableOutside: Boolean = false
    ): AlertDialog?
}