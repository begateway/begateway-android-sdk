package com.begateway.mobilepayments.utils

import androidx.core.widget.doAfterTextChanged
import com.begateway.mobilepayments.models.ui.CardType
import com.google.android.material.textfield.TextInputLayout

private const val MIN_DIGIT = 0
private const val DIGITS_MIDDLE = 5
private const val LAST_DIGIT_MASK = 10
private const val MAX_DIGIT = 9

private val DIGIT_RANGE = MIN_DIGIT..MAX_DIGIT

internal inline fun TextInputLayout.onCardNumberWatcher(
    crossinline onCardTypeUpdate: (cardType: CardType) -> Unit,
) {
    editText?.doAfterTextChanged {
        val pan = it?.toString().orEmpty().filter(Char::isDigit)
        onCardTypeUpdate(CardType.getCardTypeByPan(pan))
    }
}

internal fun String.isCorrectPan(listOfSizes: ArrayList<Int>, isLuhnCheckRequired: Boolean) =
    listOfSizes.contains(this.length) && this.filter(Char::isDigit).map(Character::getNumericValue)
        .isCorrectPan(isLuhnCheckRequired)

private fun List<Int>.isCorrectPan(isLuhnCheckRequired: Boolean) =
    all(DIGIT_RANGE::contains) && if (isLuhnCheckRequired) {
        checkCardLuhnAlgorithm(this)
    } else {
        true
    }

// Simplified version of Luhn algorithm.
// Implementation is partially taken from
// https://github.com/ErikSchierboom/exercism/blob/master/kotlin/luhn/src/main/kotlin/Luhn.kt
private fun checkCardLuhnAlgorithm(digits: List<Int>) = digits.asReversed().asSequence()
    .mapIndexed { index, digit ->
        when {
            index % 2 == 0 -> digit
            digit < DIGITS_MIDDLE -> digit * 2
            else -> digit * 2 - MAX_DIGIT
        }
    }
    .sum() % LAST_DIGIT_MASK == 0


