package com.begateway.mobilepayments.models.network.request

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.annotations.SerializedName

class Checkout(
    @SerializedName("version") val version: Double = 2.1,
    @SerializedName("test") val test: Boolean = false,
    @SerializedName("transaction_type") val transactionType: TransactionType,
    @SerializedName("attempts") val attempts: Int? = null,
    @SerializedName("dynamic_billing_descriptor") val dynamicBillingDescriptor: String? = null,
    @SerializedName("order") val order: Order,
    @SerializedName("settings") val settings: Settings,
    @SerializedName("customer") val customer: Customer? = null,
    @SerializedName("payment_method") val paymentMethod: PaymentMethod? = null,
    @SerializedName("travel") val travel: Travel? = null,
) : AdditionalFields()

class Order(
    @SerializedName("amount") val amount: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("description") val description: String,
    @SerializedName("tracking_id") val trackingId: String? = null,
    @SerializedName("expired_at") val expiredAt: String? = null,
    @SerializedName("additional_data") val additionalData: AdditionalData? = null,
) : AdditionalFields()

class AdditionalData(
    @SerializedName("receipt_text") val receiptText: Array<String>? = null,
    @SerializedName("contract") val contract: Array<Contract>? = null,
    @SerializedName("avs_cvc_verification") val avsCvcVerification: Array<String>? = null,
    @SerializedName("card_on_file") val card_on_file: CardOnFile? = null,
) : AdditionalFields()

class CardOnFile(
    @SerializedName("initiator") val initiator: Initiator = Initiator.MERCHANT,
    @SerializedName("type") val cardOnFileType: CardOnFileType = CardOnFileType.DELAYED_CHARGE,
) : AdditionalFields()

class Settings(
    /** If returnUrl defined, then successUrl and declineUrl could be ignored. Use only with header "X-Api-Version: 2" */
    @SerializedName("return_url") val returnUrl: String? = null,
    @SerializedName("success_url") val successUrl: String? = null,
    @SerializedName("decline_url") val declineUrl: String? = null,
    @SerializedName("fail_url") val failUrl: String? = null,
    @SerializedName("cancel_url") val cancelUrl: String? = null,
    @SerializedName("notification_url") val notificationUrl: String? = null,
    @SerializedName("verification_url") val verificationUrl: String? = null,
    @SerializedName("auto_return") val autoReturn: Int? = null,
    @SerializedName("button_text") val buttonText: String? = null,
    @SerializedName("button_next_text") val buttonNextText: String? = null,
    @SerializedName("language") val language: Language? = null,
    @SerializedName("customer_fields") val customerFields: CustomerFields? = null,
    @SerializedName("save_card_toggle") val saveCardPolicy: SaveCardPolicy?,
) : AdditionalFields()

class SaveCardPolicy(
    @SerializedName("customer_contract") val customerContract: Boolean?,
)

class CustomerFields(
    @SerializedName("read_only") val readOnly: Array<ReadOnly>? = null,
    @SerializedName("visible") val visible: Array<Visible>? = null,
) : AdditionalFields()

class Customer(
    @SerializedName("ip") val ip: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("zip") val zip: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("birth_date") val birthDate: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("device_id") val deviceId: String? = null,
) : AdditionalFields()

class PaymentMethod(
    @SerializedName("types") val types: Array<PaymentMethodType>,
    @SerializedName("erip") val erip: Erip? = null,
    @SerializedName("credit_card") val creditCard: CreditCard,
) : AdditionalFields()

class Erip(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("account_number") val accountNumber: String,
    @SerializedName("service_no") val serviceNo: Int? = null,
    @SerializedName("service_info") val serviceInfo: Array<String>? = null,
) : AdditionalFields()

data class CreditCard(
    @SerializedName("number") val cardNumber: String? = null,
    @SerializedName("verification_value") val cvc: String? = null,
    @SerializedName("holder") val holderName: String? = null,
    @SerializedName("exp_month") val expMonth: String? = null,
    @SerializedName("exp_year") val expYear: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("save_card") val isSaveCard: Boolean? = null,
) : AdditionalFields()

class Travel(
    @SerializedName("airline") val airline: Airline,
) : AdditionalFields()

class Airline(
    @SerializedName("agency_code") val agencyCode: String? = null,
    @SerializedName("agency_name") val agencyName: String? = null,
    @SerializedName("ticket_number") val ticketNumber: String,
    @SerializedName("booking_number") val bookingNumber: String? = null,
    @SerializedName("restricted_ticked_indicator") val restrictedTickedIndicator: RestrictedTickedIndicator? = null,
    @SerializedName("legs") val legs: Array<Leg>,
    @SerializedName("passengers") val passengers: Array<Passenger>,
) : AdditionalFields()

class Leg(
    @SerializedName("airline_code") val airlineCode: String,
    @SerializedName("stop_over_code") val stop_over_code: String? = null,
    @SerializedName("flight_number") val flightNumber: String,
    @SerializedName("departure_date_time") val departureDateTime: String,
    @SerializedName("arrival_date_time") val arrivalDateTime: String,
    @SerializedName("originating_country") val originatingCountry: String,
    @SerializedName("originating_city") val originatingCity: String,
    @SerializedName("originating_airport_code") val originatingAirportCode: String,
    @SerializedName("destination_country") val destinationCountry: String,
    @SerializedName("destination_city") val destinationCity: String,
    @SerializedName("destination_airport_code") val destinationAirportCode: String,
    @SerializedName("coupon") val coupon: String,
    @SerializedName("class") val clazz: String,
) : AdditionalFields()

class Passenger(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
) : AdditionalFields()