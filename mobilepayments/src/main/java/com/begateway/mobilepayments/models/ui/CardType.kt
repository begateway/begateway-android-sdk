package com.begateway.mobilepayments.models.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.begateway.mobilepayments.R
import java.util.regex.Pattern

internal enum class CardType(
    val cardName: String,
    private val regex: Pattern,
    @DrawableRes val drawable: Int,
    val listOfCardNumberSizesWithSpaces: ArrayList<Int>,//with spaces
    val listOfSecurityCodeSizes: ArrayList<Int>,
    @StringRes val securityCodeName: Int,
    val isLunhCheckRequired: Boolean = true,
    val maskFormat: String = "____ ____ ____ ____ ___"
) {
    BELKART(
        cardName = "belkart",
        regex = Pattern.compile("^9112"),
        drawable = R.drawable.begateway_ic_belkart,
        listOfCardNumberSizesWithSpaces = arrayListOf(19),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    PROSTIR(
        cardName = "prostir",
        regex = Pattern.compile("^9804"),
        drawable = R.drawable.begateway_ic_prostir,
        listOfCardNumberSizesWithSpaces = arrayListOf(19),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    SOLO(
        cardName = "solo",
        regex = Pattern.compile("^(6334|6767)\\d*"),
        drawable = R.drawable.begateway_ic_solo,
        listOfCardNumberSizesWithSpaces = arrayListOf(19, 22, 23),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    SWITCH(
        cardName = "switch",
        regex = Pattern.compile("^(633110|633312|633304|633303|633301|633300)\\d*"),
        drawable = R.drawable.begateway_ic_switch,
        listOfCardNumberSizesWithSpaces = arrayListOf(19, 22, 23),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    HIPERCARD(
        cardName = "hipercard",
        regex = Pattern.compile("^(384|606282|637095|637568|637599|637609|637612)\\d*"),
        drawable = R.drawable.begateway_ic_hipercard,
        listOfCardNumberSizesWithSpaces = arrayListOf(23),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    JCB(
        cardName = "jcb",
        regex = Pattern.compile("^(35[2-8]|1800|2131)\\d*"),
        drawable = R.drawable.begateway_ic_jcb,
        listOfCardNumberSizesWithSpaces = arrayListOf(18, 19, 23),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    ELO(
        cardName = "elo",
        regex = Pattern.compile(
            "^(401178|401179|431274|438935|451416|457393|457631|457632|504175|506699" +
                    "|5067[0-7]|5090[0-8]|636297|636368|650[04579]|651652|6550[0-4])\\d*"
        ),
        drawable = R.drawable.begateway_ic_elo,
        listOfCardNumberSizesWithSpaces = arrayListOf(19),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv,
        isLunhCheckRequired = false
    ),
    MASTERCARD(
        cardName = "master",
        regex = Pattern.compile("^(5[1-5]|2[3-6]|22[3-9]|222[1-9]|27[1-2])\\d*"),
        drawable = R.drawable.begateway_ic_mastercard,
        listOfCardNumberSizesWithSpaces = arrayListOf(19),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvc
    ),
    AMEX(
        cardName = "amex",
        regex = Pattern.compile("^3[47]\\d*"),
        drawable = R.drawable.begateway_ic_amex,
        listOfCardNumberSizesWithSpaces = arrayListOf(17),
        listOfSecurityCodeSizes = arrayListOf(3, 4),
        securityCodeName = R.string.begateway_cid,
        maskFormat = "____ ______ _____"
    ),
    DISCOVER(
        cardName = "discover",
        regex = Pattern.compile("^(30[0-5]|3[689]|6011[0234789]|65|64[4-9])\\d*"),
        drawable = R.drawable.begateway_ic_discover,
        listOfCardNumberSizesWithSpaces = arrayListOf(17, 19, 23),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cid
    ),
    DINERSCLUB(
        cardName = "dinersclub",
        regex = Pattern.compile("^(30[0-5]|36|38)\\d*"),
        drawable = R.drawable.begateway_ic_dinersclub,
        listOfCardNumberSizesWithSpaces = arrayListOf(17),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    MAESTRO(
        cardName = "maestro",
        regex = Pattern.compile(
            "^(500|50[2-9]|501[0-8]|5[6-9]|60[2-5]|6010|601[2-9]" +
                    "|6060|616788|6218[368]|6219[89]|622110|6220|627[1-9]" +
                    "|628[0-1]|6294|6301|630490|633857|63609|6361|636392|636708|637043|637102|637118" +
                    "|637187|637529|639|64[0-3]|67[0123457]|676[0-9]|679|6771)\\d*"
        ),
        drawable = R.drawable.begateway_ic_maestro,
        listOfCardNumberSizesWithSpaces = arrayListOf(14, 16, 17, 18, 19, 21, 22, 23),
        listOfSecurityCodeSizes = arrayListOf(0, 3),
        securityCodeName = R.string.begateway_cvc
    ),
    UNIONPAY(
        cardName = "unionpay",
        regex = Pattern.compile(
            "^(620|621[0234567]|621977|62212[6-9]|6221[3-8]" +
                    "|6222[0-9]|622[3-9]|62[3-6]|6270[2467]|628[2-4]|629[1-2]|632062|685800|69075)\\d*"
        ),
        drawable = R.drawable.begateway_ic_unionpay,
        listOfCardNumberSizesWithSpaces = arrayListOf(19, 21, 22, 23),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvn,
        isLunhCheckRequired = false
    ),
    DANKORT(
        cardName = "dankort",
        regex = Pattern.compile("^5019\\d*"),
        drawable = R.drawable.begateway_ic_dankort,
        listOfCardNumberSizesWithSpaces = arrayListOf(19),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    MIR(
        cardName = "mir",
        regex = Pattern.compile("^220[0-4]\\d*"),
        drawable = R.drawable.begateway_ic_mir,
        listOfCardNumberSizesWithSpaces = arrayListOf(19),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    VISA_ELECTRON(
        cardName = "visaelectron",
        regex = Pattern.compile("^4(026|17500|405|508|844|91[37])\\d*"),
        drawable = R.drawable.begateway_ic_visa_electron,
        listOfCardNumberSizesWithSpaces = arrayListOf(19, 23),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    VISA(
        cardName = "visa",
        regex = Pattern.compile("^4\\d*"),
        drawable = R.drawable.begateway_ic_visa,
        listOfCardNumberSizesWithSpaces = arrayListOf(16, 19, 23),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    FORBRUGSFORENINGEN(
        cardName = "forbrugsforeningen",
        regex = Pattern.compile("^600\\d*"),
        drawable = R.drawable.begateway_ic_forbrugsforeningen,
        listOfCardNumberSizesWithSpaces = arrayListOf(19),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    RUPAY(
        cardName = "rupay",
        regex = Pattern.compile(
            "^(606[1-9]|607|608|81|82|508)\\d*"
        ),
        drawable = R.drawable.begateway_ic_rupay,
        listOfCardNumberSizesWithSpaces = arrayListOf(19),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv,
        isLunhCheckRequired = false
    ),
    UNKNOWN(
        cardName = "unknown",
        regex = Pattern.compile("\\d+"),
        drawable = R.drawable.begateway_ic_unknown,
        listOfCardNumberSizesWithSpaces = arrayListOf(17, 18, 19, 21, 22, 23),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    ),
    EMPTY(
        cardName = "empty",
        regex = Pattern.compile("^$"),
        drawable = R.drawable.begateway_ic_unknown,
        listOfCardNumberSizesWithSpaces = arrayListOf(17, 18, 19, 21, 22, 23),
        listOfSecurityCodeSizes = arrayListOf(3),
        securityCodeName = R.string.begateway_cvv
    );

    companion object {
        fun getCardTypeByPan(pan: String) =
            values().find {
                it.regex.matcher(pan).matches()
            } ?: UNKNOWN
    }

    fun getMaxCardLength() = listOfCardNumberSizesWithSpaces.last()
    fun getMaxCVCLength() = listOfSecurityCodeSizes.last()
}