package com.begateway.mobilepayments.payment.samsungpay

import android.content.Context
import android.os.Bundle
import android.util.Base64
import androidx.core.os.bundleOf
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.models.network.request.Order
import com.begateway.mobilepayments.models.ui.CardType
import com.begateway.mobilepayments.utils.getFormattedAmount
import com.google.gson.JsonParser
import com.samsung.android.sdk.samsungpay.v2.PartnerInfo
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.samsungpay.v2.StatusListener
import com.samsung.android.sdk.samsungpay.v2.payment.CardInfo
import com.samsung.android.sdk.samsungpay.v2.payment.CustomSheetPaymentInfo
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountBoxControl
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountConstants
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.CustomSheet
import java.util.*

object SamsungPayHelper {
    private const val AMOUNT_CONTROL_ID = "amount_control_id"

    fun getSamsungPayClient(context: Context, serviceId: String) = SamsungPay(
        context,
        getPartnerInfo(serviceId)
    )

    fun getPaymentManager(context: Context, serviceId: String) = PaymentManager(
        context,
        getPartnerInfo(serviceId)
    )

    private fun getPartnerInfo(serviceId: String) = PartnerInfo(
        serviceId,
        bundleOf(
            SpaySdk.PARTNER_SERVICE_TYPE to SpaySdk.ServiceType.INAPP_PAYMENT.toString()
        )
    )

    internal inline fun checkSamsungStatus(
        serviceId: String,
        context: Context,
        crossinline onSuccess: () -> Unit,
        crossinline onNotReady: (() -> Unit) -> Unit,
        crossinline onUnSuccess: () -> Unit
    ) {
        getSamsungPayClient(context, serviceId).getSamsungPayStatus(
            getStatusListener(
                context,
                serviceId,
                onSuccess,
                onUnSuccess,
                onNotReady
            )
        )
    }

    internal inline fun getStatusListener(
        context: Context,
        serviceId: String,
        crossinline onSuccess: () -> Unit,
        crossinline onUnSuccess: () -> Unit,
        crossinline onNotReady: (() -> Unit) -> Unit
    ) = object : StatusListener {
        override fun onSuccess(status: Int, bundle: Bundle?) {
            when (status) {
                SamsungPay.SPAY_READY -> {
                    onSuccess()
                }
                SamsungPay.SPAY_NOT_READY -> {
                    val samsungPayClient = getSamsungPayClient(context, serviceId)
                    when (bundle?.getInt(SamsungPay.EXTRA_ERROR_REASON)) {
                        SamsungPay.ERROR_SPAY_APP_NEED_TO_UPDATE ->
                            onNotReady(
                                samsungPayClient::goToUpdatePage
                            )
                        SamsungPay.ERROR_SPAY_SETUP_NOT_COMPLETED ->
                            onNotReady(
                                samsungPayClient::activateSamsungPay
                            )
                    }
                }
                else -> onUnSuccess()
            }
        }

        override fun onFail(p0: Int, p1: Bundle?) {
            onUnSuccess()
        }

    }

    internal inline fun getCardInfoListener(
        crossinline onSuccess: () -> Unit,
        crossinline onUnSuccess: () -> Unit
    ) = object : PaymentManager.CardInfoListener {
        override fun onResult(cardResponse: MutableList<CardInfo>?) {
            val isCardPresented = cardResponse?.find { cardInfo ->
                CardType.values().find { cardPayment ->
                    getSamsungCardBrand(cardPayment) == cardInfo.brand
                } != null
            } != null
            if (isCardPresented) {
                onSuccess()
            } else {
                onUnSuccess()
            }
        }

        override fun onFailure(p0: Int, p1: Bundle?) {
            onUnSuccess()
        }
    }

    fun startInAppPayWithCustomSheet(
        context: Context,
        order: Order,
        listener: PaymentManager.CustomSheetTransactionInfoListener,
        serviceId: String
    ) {
        getPaymentManager(context, serviceId).startInAppPayWithCustomSheet(
            makeCustomSheetPaymentInfo(context, order),
            listener
        )
    }

    private fun makeCustomSheetPaymentInfo(
        context: Context,
        order: Order
    ): CustomSheetPaymentInfo? {
        return CustomSheetPaymentInfo.Builder()
            .setMerchantName(
                context.getString(R.string.begateway_app_name)
            )
            .setOrderNumber("1")
            .setAddressInPaymentSheet(CustomSheetPaymentInfo.AddressInPaymentSheet.DO_NOT_SHOW)
            .setAllowedCardBrands(emptyList())
            .setCardHolderNameEnabled(true)
            .setRecurringEnabled(false)
            .setCustomSheet(
                CustomSheet().apply {
                    addControl(makeAmountBoxControl(order))
                }
            )
            .build()
    }

    @Throws(IllegalStateException::class, NullPointerException::class)
    fun updateCustomSheet(context: Context, serviceId: String, customSheet: CustomSheet?) {
        getPaymentManager(context, serviceId).updateSheet(customSheet)
    }

    private fun makeAmountBoxControl(order: Order): AmountBoxControl {
        val currency = Currency.getInstance(order.currency)
        return AmountBoxControl(AMOUNT_CONTROL_ID, currency.currencyCode).apply {
            setAmountTotal(
                order.amount.getFormattedAmount(currency).toDouble(),
                AmountConstants.FORMAT_TOTAL_PRICE_ONLY
            )
        }
    }

    internal fun getSamsungCardBrand(cardType: CardType): SpaySdk.Brand? =
        when (cardType) {
            CardType.VISA -> SpaySdk.Brand.VISA
            CardType.MASTERCARD -> SpaySdk.Brand.MASTERCARD
            CardType.AMEX -> SpaySdk.Brand.AMERICANEXPRESS
            CardType.DISCOVER -> SpaySdk.Brand.DISCOVER
            else -> null
        }

    internal fun getCardTypeBySamsungCardBrand(brand: SpaySdk.Brand): CardType? =
        when (brand) {
            SpaySdk.Brand.AMERICANEXPRESS -> CardType.AMEX
            SpaySdk.Brand.MASTERCARD -> CardType.MASTERCARD
            SpaySdk.Brand.DISCOVER -> CardType.DISCOVER
            SpaySdk.Brand.VISA -> CardType.VISA
            else -> null
        }

    internal fun extractSamsungDataBlock(paymentCredentials: String): String? =
        JsonParser().parse(paymentCredentials)?.asJsonObject?.let { data ->
            if (data.isJsonNull) {
                null
            } else {
                data.getAsJsonObject("3DS")
                    .getAsJsonPrimitive("data").asString
            }
        }

    internal fun encodeBase64(token: String) =
        String(Base64.encode(token.toByteArray(), Base64.NO_WRAP))
}