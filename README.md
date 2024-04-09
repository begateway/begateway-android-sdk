# beGateway components for Android
beGateway components for Android allows you to accept in-app payments

# Integration

## Installation

The Components are available through 
[![](https://jitpack.io/v/begateway/begateway-android-sdk.svg)](https://jitpack.io/#begateway/begateway-android-sdk), you only need to add the Gradle dependency.

### Import with Gradle

Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
```

Import the component module by adding it to your `build.gradle` file.

```groovy
   implementation 'com.github.begateway:begateway-android-sdk:2.0.0'
```
You can give a look to the full Kotlin sample by clicking [here](https://github.com/begateway/begateway-android-sdk/tree/master/app)

## Usage

### Setup

Initilize payment module:

Don't forget to add permissions to your manifest:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```
init of `PaymentSdk` class
```kotlin
PaymentSdkBuilder().apply {
        setDebugMode(BuildConfig.DEBUG)
        setPublicKey(TestData.PUBLIC_STORE_KEY_3D)
        setCardNumberFieldVisibility(true)
        setCardCVCFieldVisibility(false)
        setCardDateFieldVisibility(true)
        setCardHolderFieldVisibility(true)
        setNFCScanVisibility(true)
        setEndpoint(YOUR_CHECKOUT_ENDPOINT)
    }.build()
```
in `PaymentSdkBuilder` you can find full list for of properties

### Implement OnResultListener
```kotlin
class MainActivity : AppCompatActivity(), OnResultListener {

    //here you will receive payment token
    override fun onTokenReady(token: String) {
    }
    
    //here you will receive final status of payment and token of credit card
    override fun onPaymentFinished(beGatewayResponse: BeGatewayResponse, cardToken: String?) {
    }
}
```
BeGatewayResponse parameters:
```kotlin
    val status: ResponseStatus
    //SUCCESS("successful"),
    //ERROR("error"),
    //CANCELED("canceled"),
    //INCOMPLETE("incomplete"),
    //FAILED("failed"),
    //TIME_OUT("time_out");
    
    val message: String?     //details message about `ResponseStatus`
```
### Start payment

#### You can receive payment token
```kotlin
            sdk.getPaymentToken(
                TokenCheckoutData(
                    Checkout(
                        test = BuildConfig.DEBUG,// true only if you work in test mode
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
                            )
                        ),
                        settings = Settings(
                            returnUrl = "https://DEFAULT_RETURN_URL.com",
                            autoReturn = 0,
                        ),
                    ),
                ).apply {
                    addCustomField("customField", "custom string")
                }
            )
```
### With this payment token you can do:
#### 1. Start payment with card form
```kotlin
            startActivity(
                PaymentSdk.getCardFormIntent(this@MainActivity)
            )
```
#### 2. Start payment with `CheckoutWithTokenData`
here you also can use payment token from your server or etc.

```kotlin
    private fun payWithCheckout() {
        sdk.checkoutWithTokenData = CheckoutWithTokenData(
            CheckoutWithToken(
                token = token
            )
        )
        startActivity(
            PaymentSdk.getCardFormIntent(this@MainActivity)
        )
    }
```
#### 3. To make a payment using a saved card, you must initialize the SDK, receive a payment token, then turn on the "Saver card" switch when paying with the card. The card token will be saved, then you can call the UI of the saved cards using the function:
```kotlin
   private fun payWithCard() {
        val token = binding.tilToken.editText?.text?.toString() ?: return
        startActivity( PaymentSdk.getSaveCardIntent(this@MainActivity, token))
    }
```
## Dynamic Fields
To any object of request you can add dynamic field by using methods that
provide `AdditionalFields` class.
```kotlin
        Checkout(
            test = true,
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
            ).apply {
                addCustomField("settingsCustomIntKey", 14006)
            },
        ).apply {
            addCustomField("checkoutCustomStrKey", "checkoutCustomStrValue")
        }
```
 You have access to our serializer `CustomSerializer` and `AdditionalFields`.
## Encryption
Use PaymentSdk.encryptData(data: String) to get encrypted credit card data

For example:
```kotlin
val encryptedCardNumber = sdk.encryptData("4200000000000000")
val encryptedHolder = sdk.encryptData("IVAN IVANOV")
val encryptedCvv = sdk.encryptData("123")
```

## Customizing
#### Customize using styles.xml
You can rewrite main style for card form view elements in your styles.xml

```xml
    <style name="begateway_Theme" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
        <!-- Primary brand color. -->
        <item name="colorPrimary">@color/purple_200</item>
        <item name="colorPrimaryVariant">@color/purple_700</item>
        <item name="colorOnPrimary">@color/black</item>
        <!-- Secondary brand color. -->
        <item name="colorSecondary">@color/teal_200</item>
        <item name="colorSecondaryVariant">@color/teal_200</item>
        <item name="colorOnSecondary">@color/black</item>
        <!-- Status bar color. -->
        <item name="android:statusBarColor" tools:targetApi="l">?attr/colorPrimaryVariant</item>
        <!-- Customize your theme here. -->
    </style>
```
#### Customize using string.xml
You can rewrite string for card form view elements in your string.xml, just put in your project string with same names but other values
```xml
<resources>
    <string name="begateway_form_hint_card_number">Card number</string>
    <string name="begateway_form_hint_expiration">Expiration date</string>
    <string name="begateway_form_hint_cardholder_name">Name on card</string>
</resources>
```

#### Customize using dimens.xml
You can rewrite dimens for text size of fields
```xml
<resources>
    <dimen name="begateway_card_number_text_size">16sp</dimen>
    <dimen name="begateway_card_holder_text_size">16sp</dimen>
    <dimen name="begateway_card_expire_date_text_size">16sp</dimen>
    <dimen name="begateway_card_cvc_text_size">16sp</dimen>
</resources>
```

## Bank card scanning
### NFC
if device has nfc sdk will show to user button for scanning
### By Camera
you can choose any library that you want to use, just add to your manifest `ScanBankCardActivity` activity with our action
```xml
        <activity android:name=".ScanBankCardActivity">
            <intent-filter>
                <action android:name="com.begateway.mobilepayments.action.SCAN_BANK_CARD"/>
            </intent-filter>
        </activity>
```
and implement logic of library that you've chosen and when you will have a result send to our sdk
```kotlin
                setResult(
                      Activity.RESULT_OK,
                      PaymentSdk.getCardDataIntent(
                          scanResult.cardNumber,
                          scanResult.cardholderName,
                          scanResult.expiryMonth.toString(),//02
                          scanResult.expiryYear.toString(),//2021 or 21
                          scanResult.cvv                   // or you can use getCardDataIntentWithExpiryString() where expiryString can be 02/21 or 02/2021
                      )
                  )
                  finish()
```
## Google Pay

if you want pay with google pay add this to your manifest
```xml
        <meta-data
            android:name="com.google.android.gms.wallet.api.enabled"
            android:value="true" />
```
## License

This repository is open source and available under the MIT license. For more information, see the LICENSE file.
