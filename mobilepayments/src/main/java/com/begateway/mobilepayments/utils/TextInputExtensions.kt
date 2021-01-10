package com.begateway.mobilepayments.utils

import android.text.InputFilter
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

internal inline fun TextInputLayout.onFocusListener(
    crossinline isCorrect: () -> Boolean,
    errorString: String
) {
    setOnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            this.error = if (isCorrect()) {
                null
            } else {
                errorString
            }
        }
    }

}

internal inline fun TextInputLayout.onTextChanged(
    crossinline updateStates: () -> Unit,
    crossinline changeFocus: (editText: EditText) -> Boolean,
    maxLength: Int?
) {
    editText?.doAfterTextChanged {
        error = null
        if (!it.isNullOrEmpty() && it.length == maxLength) {
            editText?.let { editText -> changeFocus(editText) }
        }
        updateStates()
    }
}

internal inline fun EditText.onEditorListener(
    crossinline changeFocus: (editText: EditText) -> Boolean,
) {
    setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
            EditorInfo.IME_ACTION_NEXT -> {
                changeFocus(this)
            }
            else -> false
        }
    }
}

internal fun EditText.configureForCardNumberInput(maskFormatWatcher: MaskFormatWatcher): Unit =
    maskFormatWatcher.installOn(this)

internal fun inputFilterDigits(regex: String) =
    InputFilter { source, start, end, _, _, _ ->
        source.subSequence(start, end).toString().replace(regex.toRegex(), "")
            .trim { it <= ' ' }
    }

internal fun maskFormatWatcher(mask: String) = MaskFormatWatcher(
    getMaskImpl(mask)
)

internal fun getMaskImpl(mask: String) = MaskImpl.createTerminated(
    UnderscoreDigitSlotsParser()
        .parseSlots(mask)
)