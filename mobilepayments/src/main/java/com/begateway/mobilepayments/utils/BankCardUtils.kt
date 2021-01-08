package com.begateway.mobilepayments.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.begateway.mobilepayments.R
import java.util.regex.Pattern

private const val MIN_DIGIT = 0
private const val DIGITS_MIDDLE = 5
private const val LAST_DIGIT_MASK = 10
private const val MAX_DIGIT = 9

private val DIGIT_RANGE = MIN_DIGIT..MAX_DIGIT

internal fun String.isCorrectPan(range: IntRange) =
    (this.length in range) && this.filter(Char::isDigit).map(Character::getNumericValue).isCorrectPan()

private fun List<Int>.isCorrectPan() =
    all(DIGIT_RANGE::contains) && checkCardLuhnAlgorithm(this)

// Simplified version of Luhn algorithm.
// Implementation is partially taken from
private fun checkCardLuhnAlgorithm(digits: List<Int>) = digits.asReversed().asSequence()
    .mapIndexed { index, digit ->
        when {
            index % 2 == 0 -> digit
            digit < DIGITS_MIDDLE -> digit * 2
            else -> digit * 2 - MAX_DIGIT
        }
    }
    .sum() % LAST_DIGIT_MASK == 0

enum class CardType(
    val regex: Pattern,
    @DrawableRes val drawable: Int,
    val minCardLength: Int,
    val maxCardLength: Int,
    val securityCodeLength: Int,
    @StringRes val securityCodeName: Int
) {
    MIR(
        Pattern.compile("^220[0-4]"),
        R.drawable.begateway_ic_mir,
        19,
        19,
        3,
        R.string.begateway_cvv
    ),
    BELKART(
        Pattern.compile("^9112"),
        R.drawable.begateway_ic_belkart,
        19,
        19,
        3,
        R.string.begateway_cvv
    ),
    VISA(
        Pattern.compile("^4\\d*"),
        R.drawable.begateway_ic_visa,
        19,
        19,
        3,
        R.string.begateway_cvv
    ),
    MASTERCARD(
        Pattern.compile("^(5[1-5]|222[1-9]|22[3-9]|2[3-6]|27[0-1]|2720)\\d*"),
        R.drawable.begateway_ic_mastercard,
        19,
        19,
        3,
        R.string.begateway_cvc
    ),
    DISCOVER(
        Pattern.compile("^(6011|65|64[4-9]|622)\\d*"),
        R.drawable.begateway_ic_discover,
        19,
        19,
        3,
        R.string.begateway_cid
    ),
    AMEX(
        Pattern.compile("^3[47]\\d*"),
        R.drawable.begateway_ic_amex,
        18,
        18,
        4,
        R.string.begateway_cid
    ),
    DINERSCLUB(
        Pattern.compile("^(36|38|30[0-5])\\d*"),
        R.drawable.begateway_ic_dinersclub,
        17,
        17,
        3,
        R.string.begateway_cvv
    ),
    JCB(
        Pattern.compile("^35\\d*"),
        R.drawable.begateway_ic_jcb,
        19,
        19,
        3,
        R.string.begateway_cvv
    ),
    MAESTRO(
        Pattern.compile("^(5077|5049|5018|5020|5038|5[6-9]|6020|6304|6703|6759|676[1-3])\\d*"),
        R.drawable.begateway_ic_maestro,
        14,
        23,
        3,
        R.string.begateway_cvc
    ),
    UNIONPAY(
        Pattern.compile("^62\\d*"),
        R.drawable.begateway_ic_unionpay,
        19,
        23,
        3,
        R.string.begateway_cvn
    ),
    UNKNOWN(
        Pattern.compile("\\d+"),
        R.drawable.begateway_ic_unknown,
        14,
        23,
        3,
        R.string.begateway_cvv
    ),
    EMPTY(
        Pattern.compile("^$"),
        R.drawable.begateway_ic_unknown,
        14,
        23,
        3,
        R.string.begateway_cvv
    );

    companion object {
        internal fun getCardTypeByPan(pan: String) =
            values().find {
                it.regex.matcher(pan).matches()
            } ?: UNKNOWN
    }
}
