package com.begateway.mobilepayments.model.network.request

import com.begateway.mobilepayments.model.Initiator
import com.begateway.mobilepayments.model.TransactionType
import com.begateway.mobilepayments.model.Type
import com.google.gson.annotations.SerializedName

internal class Checkout(
    @SerializedName("version") val version: Double = 2.1,
    @SerializedName("test") val test: Boolean = false,
    @SerializedName("transaction_type") val transactionType: TransactionType,
    @SerializedName("attempts") val attempts: Int = 1,
    @SerializedName("dynamic_billing_descriptor") val dynamicBillingDescriptor: String? = null,
    @SerializedName("order") val order: Order,
) : AdditionalFields()

internal class Order(
    @SerializedName("amount") val amount: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("description") val description: String,
    @SerializedName("tracking_id") val trackingId: String? = null,//todo
    @SerializedName("expired_at") val expiredAt: String? = null,//todo
    @SerializedName("additional_data") val additionalData: AdditionalData? = null,
) : AdditionalFields()

internal class AdditionalData(
    @SerializedName("receipt_text") val receiptText: Array<String>? = null,
    @SerializedName("contract") val contract: Array<String>? = null,
    @SerializedName("avs_cvc_verification") val avsCvcVerification: Array<String>? = null,
    @SerializedName("card_on_file") val card_on_file: CardOnFile? = null,
) : AdditionalFields()

internal class CardOnFile(
    @SerializedName("initiator") val initiator: Initiator = Initiator.MERCHANT,
    @SerializedName("type") val type: Type = Type.DELAYED_CHARGE,
)
