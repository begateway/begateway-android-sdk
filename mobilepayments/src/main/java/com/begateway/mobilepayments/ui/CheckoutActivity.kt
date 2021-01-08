package com.begateway.mobilepayments.ui

import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.begateway.mobilepayments.utils.applyTintColorOnDrawable

private val TAG_CARD_FORM_SHEET = CardFormBottomDialog::class.java.name

class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CardFormBottomDialog().show(supportFragmentManager, TAG_CARD_FORM_SHEET)
    }

    fun setToolBar(
        toolbar: Toolbar,
        @ColorRes color: Int,
        @StringRes string: Int? = null,
        onBackPressedAction: (() -> Unit)?
    ) {
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = string?.let { getString(it) }
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            toolbar.navigationIcon?.applyTintColorOnDrawable(
                ContextCompat.getColor(
                    this@CheckoutActivity,
                    color
                )
            )
            toolbar.setNavigationOnClickListener {
                onBackPressedAction?.invoke() ?: onBackPressed()
            }
        }
    }
}