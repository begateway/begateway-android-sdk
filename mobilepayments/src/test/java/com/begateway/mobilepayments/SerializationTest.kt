package com.begateway.mobilepayments

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.begateway.mobilepayments.models.network.CheckoutWithToken
import com.begateway.mobilepayments.models.network.request.*
import com.begateway.mobilepayments.parser.CustomSerializer
import com.google.gson.*
import org.junit.Test

class SerializationTest {
    private val classesWithAdditionalFields = arrayOf(
        Checkout::class.java,
        Order::class.java,
        AdditionalData::class.java,
        CardOnFile::class.java,
        Settings::class.java,
        CustomerFields::class.java,
        Customer::class.java,
        PaymentMethod::class.java,
        Erip::class.java,
        CreditCard::class.java,
        Travel::class.java,
        Airline::class.java,
        Leg::class.java,
        Passenger::class.java,
        CheckoutWithToken::class.java,
        Request::class.java
    )

    @Test
    fun serialize() {
        val serializer = CustomSerializer<AdditionalFields>()
        val builder = GsonBuilder()
        classesWithAdditionalFields.forEach {
            builder.registerTypeAdapter(it, serializer)
        }
        val gson = builder
            .setPrettyPrinting()
            .create()
        val result = gson.toJson(getEntity())
        println(result)
    }

    private fun getEntity(): Any {
        return Checkout(
            test = true,// true only if you work in test mode
            transactionType = TransactionType.PAYMENT,
            order = Order(
                amount = 100,
                currency = "USD",
                description = "Payment description",
                trackingId = "merchant_id",
                additionalData = AdditionalData(
                    contract = arrayOf(
                        Contract.RECURRING,
                        Contract.CARD_ON_FILE
                    )
                ).apply {
                    addCustomField("orderCustomBooleanKey", true)
                }
            ).apply {
                    addCustomJsonObject(JsonObject().apply {
                        add("toOrder", JsonArray().apply {
                            add("str")
                            add("str2")
                            add("str3")
                            add(true)
                        })
                    })
            },
            settings = Settings(
                returnUrl = "https://DEFAULT_RETURN_URL.com",
                autoReturn = 0,
                saveCardPolicy = SaveCardPolicy(
                    true
                )
            ).apply {
                addCustomField("settingsCustomIntKey", 14006)
            },
        ).apply {
            addCustomField("checkoutCustomStrKey", "checkoutCustomStrValue")
        }
    }

    @Test
    fun testPrimitive() {
        val primitive = JsonPrimitive(true)
        println(Gson().toJson(primitive))
    }

    @Test
    fun testArray() {
        val array = JsonArray()
        array.add(true)
        array.add(4)
        array.add("someString")
        println(Gson().toJson(array))
    }

    @Test
    fun testNull() {
        val jsonNull = JsonNull.INSTANCE
        println(Gson().toJson(jsonNull))
    }
}