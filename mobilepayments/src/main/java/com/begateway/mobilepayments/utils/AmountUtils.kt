package com.begateway.mobilepayments.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

fun Long.getFormattedAmount(currency: Currency): String {
    return BigDecimal(this)
        .divide(BigDecimal(100))
        .setScale(currency.defaultFractionDigits.takeIf { it != -1 } ?: 0, RoundingMode.HALF_EVEN)
        .toString()
}