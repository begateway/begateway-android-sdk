package com.begateway.mobilepayments.models.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.begateway.mobilepayments.R
import java.util.regex.Pattern

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
        Pattern.compile("^220\\d*"),
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
    VISA_ELECTRON(
        "visaelectron",
        Pattern.compile("^4(026|17500|405|508|844|91[37])\\d*"),
        R.drawable.begateway_ic_visa_electron,
        arrayListOf(19, 23),
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
    MASTERCARD(
        "master",
        Pattern.compile("^(5[1-5]|2[3-6]|222[1-9]|27[1-2])\\d*"),
        R.drawable.begateway_ic_mastercard,
        arrayListOf(19),
        arrayListOf(3),
        R.string.begateway_cvc
    ),
    DISCOVER(
        "discover",
        Pattern.compile("^(6011|65|64[4-9])\\d*"),
        R.drawable.begateway_ic_discover,
        arrayListOf(19, 23),
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
        Pattern.compile("^(30[0-5]|36|38)\\d*"),
        R.drawable.begateway_ic_dinersclub,
        arrayListOf(17),
        arrayListOf(3),
        R.string.begateway_cvv
    ),
    JCB(
        "jcb",
        Pattern.compile("^35\\d*"),
        R.drawable.begateway_ic_jcb,
        arrayListOf(18, 19),
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
        Pattern.compile(
            "^(500|50[2-9]|501[0-8]|56|57|58|60[2-5]|6010|601[2-9]" +
                    "|6060|616788|6218[368]|6219[89]|622110|6220|627[1-9]" +
                    "|628[0-1]|6294|6301|630490|633857|63609|6361|636392|636708|637043|637102|637118" +
                    "|637187|637529|639|64[0-3]|67[0123457]|676[0-9]|679|6771)\\d*"
        ),
        R.drawable.begateway_ic_maestro,
        arrayListOf(14, 16, 17, 18, 19, 21, 22, 23),
        arrayListOf(0, 3),
        R.string.begateway_cvc
    ),
    UNIONPAY(
        "unionpay",
        Pattern.compile(
            "^(620|621[0234567]|621977|62212[6-9]|6221[3-8]" +
                    "|6222[0-9]|622[3-9]|62[3-6]|6270[2467]|628[2-4]|629[1-2]|632062|685800|69075)\\d*"
        ),
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