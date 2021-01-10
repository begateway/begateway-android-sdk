package com.begateway.mobilepayments.utils

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputLayout

private const val MAXIMUM_VALID_YEAR_DIFFERENCE = 21
internal fun TextInputLayout.onExpiryTextChanged(year: Int) {
    editText?.addTextChangedListener(object : TextWatcher {
        private var previousValue: String? = null
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            var value = s.toString()

            if (value == previousValue) {
                return
            }
            previousValue = value
            value = value.filter(Char::isDigit)
            val length = value.length
            value = when {
                length > 0 -> when (value[0]) {
                    '0' -> {
                        if (length > 1 && value[1] == '0') {
                            value.substring(0, 1)
                        }
                        format(value)
                    }
                    '1' -> if (length > 1) {
                        when (value[1]) {
                            '0', '1', '2' -> format(value)
                            else -> value.substring(0, 1)
                        }
                    } else {
                        value
                    }
                    else -> if (length == 1) "0$value" else ""
                }
                else -> value
            }
            s?.replace(0, s.length, value)
        }

        private fun format(value: String): String {
            val length = value.length
            return if (length > 2) {
                value.substring(0, 2) + '/' + value.substring(2, length)
            } else {
                value
            }
        }
    })
}