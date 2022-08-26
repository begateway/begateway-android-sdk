package com.begateway.mobilepayments.utils

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import ru.tinkoff.decoro.MaskDescriptor
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.DescriptorFormatWatcher

internal inline fun TextInputLayout.onFocusListener(
    crossinline isCorrect: () -> Boolean,
    crossinline onError: () -> String
) {
    editText?.setOnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            error = if (isCorrect()) {
                null
            } else {
                onError()
            }
        }
    }
}

internal inline fun TextInputLayout.onTextChanged(
    crossinline updateViewStates: () -> Unit,
    noinline changeFocus: (editText: EditText) -> Boolean,
    crossinline isMaxLengthAccepted: (length: Int) -> Boolean,
) {
    editText?.doAfterTextChanged {
        if (editText?.hasFocus() == true)
            error = null
        if (!it.isNullOrEmpty() && isMaxLengthAccepted(it.length)) {
            editText?.let { editText -> changeFocus(editText) }
        }
        updateViewStates()
    }
}

internal fun EditText.onEditorListener(
    changeFocus: (editText: EditText) -> Boolean,
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

internal fun inputFilterDigits(regex: String) =
    InputFilter { source, start, end, _, _, _ ->
        source.subSequence(start, end).toString().replace(regex.toRegex(), "")
            .trim { it <= ' ' }
    }

internal fun EditText.installMask(maskFormatWatcher: DescriptorFormatWatcher): Unit =
    maskFormatWatcher.installOn(this)


internal fun maskFormatWatcher(mask: String) = DescriptorFormatWatcher(
    getMaskDescriptor(mask)
)

internal fun getMaskDescriptor(mask: String) = MaskDescriptor.ofSlots(
    UnderscoreDigitSlotsParser()
        .parseSlots(mask)
)

private const val NEW_LINE = "\n"
private const val TABULATION = "\t"
private const val EMPTY_STRING = ""

internal inline fun EditText.addDuplicateSpacesTextWatcher(
    crossinline beforeTextChanged: (
        text: CharSequence?,
        start: Int,
        count: Int,
        after: Int
    ) -> Unit = { _, _, _, _ -> },
    crossinline onTextChanged: (
        text: CharSequence?,
        start: Int,
        before: Int,
        count: Int
    ) -> Unit = { _, _, _, _ -> },
    crossinline afterTextChanged: (text: String) -> Unit = {}
): TextWatcher = object : TextWatcher {
    private val regex = "\\s+".toRegex()
    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
        beforeTextChanged.invoke(text, start, count, after)
    }

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        onTextChanged.invoke(text, start, before, count)
    }

    override fun afterTextChanged(s: Editable?) {
        var text = s?.toString().orEmpty()
        if (text.isNotEmpty()) {
            text = text.trimStart().replace(regex) {
                val values = it.value.toList().distinct().joinToString(EMPTY_STRING)
                when {
                    values.contains(NEW_LINE) -> {
                        NEW_LINE
                    }
                    values.contains(TABULATION) -> {
                        TABULATION
                    }
                    else -> values
                }
            }
        }
        removeTextChangedListener(this)
        setTextKeepState(text)
        afterTextChanged(text)
        addTextChangedListener(this)
    }
}.also { watcher ->
    addTextChangedListener(watcher)
}