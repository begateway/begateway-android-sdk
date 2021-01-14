package com.begateway.mobilepayments.ui

import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.begateway.mobilepayments.utils.applyTintColorOnDrawable

internal abstract class AbstractActivity : AppCompatActivity() {
    fun setToolBar(
        toolbar: Toolbar,
        @ColorInt navIconColor: Int,
        @ColorInt toolbarBackgroundColor: Int,
        string: String? = null,
        onBackPressedAction: (() -> Unit)?
    ) {
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = string
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            toolbar.navigationIcon?.applyTintColorOnDrawable(navIconColor)
            toolbar.setNavigationOnClickListener {
                onBackPressedAction?.invoke() ?: onBackPressed()
            }
            toolbar.setBackgroundColor(toolbarBackgroundColor)
        }
    }
}