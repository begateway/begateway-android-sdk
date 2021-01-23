package com.begateway.mobilepayments.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import com.begateway.mobilepayments.R
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

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

internal enum class CardType(
    val cardName: String,
    private val regex: Pattern,
    @DrawableRes val drawable: Int,
    val listOfCardNumberSizes: ArrayList<Int>,//with spaces
    val listOfSecurityCodeSizes: ArrayList<Int>,
    @StringRes val securityCodeName: Int,
    val isLunhCheckRequired: Boolean = true,
    val maskFormat: String = "____ ____ ____ ____ ___"
) {
    MIR(
        "mir",
        Pattern.compile("^220[1-4]"),
        R.drawable.begateway_ic_mir,
        arrayListOf(19),
        arrayListOf(3),
        R.string.begateway_cvv
    ),
    BELKART(
        "belkart",
        Pattern.compile("^9112"),
        R.drawable.begateway_ic_belkart,
        arrayListOf(19),
        arrayListOf(3),
        R.string.begateway_cvv
    ),
    VISA(
        "visa",
        Pattern.compile("^4\\d*"),
        R.drawable.begateway_ic_visa,
        arrayListOf(16, 19, 23),
        arrayListOf(3),
        R.string.begateway_cvv
    ),
    VISA_ELECTRON(
        "visaelectron",
        Pattern.compile("^4(026|17500|405|508|844|91[37])\\d*"),
        R.drawable.begateway_ic_visa_electron,
        arrayListOf(19, 23),
        arrayListOf(3),
        R.string.begateway_cvv
    ),
    MASTERCARD(
        "master",
        Pattern.compile("^(5[1-5]|2[3-6]|222|27[1-2])\\d*"),
        R.drawable.begateway_ic_mastercard,
        arrayListOf(19),
        arrayListOf(3),
        R.string.begateway_cvc
    ),
    DISCOVER(
        "discover",
        Pattern.compile("^6([045]|22)\\d*"),
        R.drawable.begateway_ic_discover,
        arrayListOf(19),
        arrayListOf(3),
        R.string.begateway_cid
    ),
    AMEX(
        "amex",
        Pattern.compile("^3[47]\\d*"),
        R.drawable.begateway_ic_amex,
        arrayListOf(17),
        arrayListOf(3, 4),
        R.string.begateway_cid,
        maskFormat = "____ ______ _____"
    ),
    DINERSCLUB(
        "dinersclub",
        Pattern.compile("^3[0689]\\d*"),
        R.drawable.begateway_ic_dinersclub,
        arrayListOf(17),
        arrayListOf(3),
        R.string.begateway_cvv
    ),
    JCB(
        "jcb",
        Pattern.compile("^35\\d*"),
        R.drawable.begateway_ic_jcb,
        arrayListOf(19),
        arrayListOf(3),
        R.string.begateway_cvv
    ),
    DANKORT(
        "dankort",
        Pattern.compile("^5019\\d*"),
        R.drawable.begateway_ic_dankort,
        arrayListOf(19),
        arrayListOf(3),
        R.string.begateway_cvv
    ),
    FORBRUGSFORENINGEN(
        "forbrugsforeningen",
        Pattern.compile("^600\\d*"),
        R.drawable.begateway_ic_forbrugsforeningen,
        arrayListOf(19),
        arrayListOf(3),
        R.string.begateway_cvv
    ),
    MAESTRO(
        "maestro",
        Pattern.compile("^(5(018|0[23]|[68])|6(39|7))\\d*"),
        R.drawable.begateway_ic_maestro,
        arrayListOf(14, 16, 17, 18, 19, 21, 22, 23),
        arrayListOf(0, 3),
        R.string.begateway_cvc
    ),
    UNIONPAY(
        "unionpay",
        Pattern.compile("^(62|88)\\d*"),
        R.drawable.begateway_ic_unionpay,
        arrayListOf(19, 21, 22, 23),
        arrayListOf(3),
        R.string.begateway_cvn,
        false
    ),
    UNKNOWN(
        "unknown",
        Pattern.compile("\\d+"),
        R.drawable.begateway_ic_unknown,
        arrayListOf(17, 18, 19, 21, 22, 23),
        arrayListOf(3),
        R.string.begateway_cvv
    ),
    EMPTY(
        "empty",
        Pattern.compile("^$"),
        R.drawable.begateway_ic_unknown,
        arrayListOf(17, 18, 19, 21, 22, 23),
        arrayListOf(3),
        R.string.begateway_cvv
    );

    companion object {
        fun getCardTypeByPan(pan: String) =
            values().find {
                it.regex.matcher(pan).matches()
            } ?: UNKNOWN
    }

    fun getMaxCardLength() = listOfCardNumberSizes.last()
    fun getMaxCVCLength() = listOfSecurityCodeSizes.last()
}
