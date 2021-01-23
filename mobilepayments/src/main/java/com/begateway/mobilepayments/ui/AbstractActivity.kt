package com.begateway.mobilepayments.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.utils.applyTintColorOnDrawable

internal abstract class AbstractActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        super.onCreate(savedInstanceState)
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
        string: String? = null,
        onBackPressedAction: (() -> Unit)? = { onBackPressed() }
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