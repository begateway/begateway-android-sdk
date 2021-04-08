package com.begateway.mobilepayments.ui.intefaces

import androidx.appcompat.widget.Toolbar

interface OnActionbarSetup {
    fun addToolBar(
        toolbar: Toolbar,
        title: String?,
        onBackPressedAction: (() -> Unit)?
    )
}