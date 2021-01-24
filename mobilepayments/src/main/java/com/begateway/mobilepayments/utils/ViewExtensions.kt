package com.begateway.mobilepayments.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.begateway.mobilepayments.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal fun View.showSoftKeyboard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

internal fun View.hideSoftKeyboard() {
    (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.also { imm ->
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}

internal fun getProgressDialog(
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