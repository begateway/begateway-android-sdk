package com.begateway.mobilepayments.utils

import android.text.InputFilter
import android.widget.EditText
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

internal fun EditText.configureForCardNumberInput(): Unit = UnderscoreDigitSlotsParser()
    .parseSlots("____ ____ ____ ____ ___")
    .let { MaskFormatWatcher(MaskImpl.createTerminated(it)) }
    .installOn(this)

internal fun inputFilter(regex: String) =
    InputFilter { source, start, end, _, _, _ ->
        source.subSequence(start, end).toString().replace(regex.toRegex(), "")
            .trim { it <= ' ' }
    }