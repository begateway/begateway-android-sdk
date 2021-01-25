package com.begateway.mobilepayments.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

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
