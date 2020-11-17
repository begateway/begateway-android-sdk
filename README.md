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
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Import the component module by adding it to your `build.gradle` file.

```groovy
   implementation 'com.github.begateway:begateway-android-sdk:1.0.6'
```
You can give a look to the full java sample by clicking [here](https://github.com/begateway/begateway-android-sdk/tree/master/samplejava)

## Usage

### 1) Setup

Initilize payment module:
```java
PaymentModule paymentModule = new PaymentBuilder()
    .setEndpoint(YOUR_CHECKOUT_ENDPOINT)
    .setPaymentResultListener(MainActivity.this)
    .build(getApplicationContext(), MainActivity.this);
```
Example YOUR_CHECKOUT_ENDPOINT = "https://checkout.begateway.com/ctp/api/"

You can setup your return_url to process 3D SECURE:
```java
    .setReturnUrl("https://YOUR_RETURN_URL.com")
```
You can setup your notification_url to process transactions on your backend:
```java
    .setNotificationUrl("https://YOUR_NOTIFICATION_URL.com")
```

You can setup trasaction type. Use one of valid types: PAYMENT, AUTHORIZATION, VERIFY:
```java
    .setTransactionType(TransactionType.PAYMENT)
```

Don't forget to add premissions to your manifest:
```java
<uses-permission android:name="android.permission.INTERNET" />
```

### 2) Implement OnPaymentResultListener
```java 
public class MainActivity extends AppCompatActivity implements OnPaymentResultListener{

    @Override
    public void onPaymentResult(PaymentResultResponse paymentResult) {
      // process payment callback
    }
}
```
PaymentResultResponse parameters:
```java
* ResponseCode getStatus() // Status of request ResponseCode.SUCCESS, ResponseCode.CANCELED, ResponseCode.ERROR, ResponseCode.TIMEOUT
* boolean isSaveCard() // User toggle state of save card checkbox
* String getTokenCard() // Get card token when payment was successful
* String getPaymentStatus() // Payment status that returned in payment response
* int getResponseCode() // Get response code 200 - success
* String getError() // Get error during request. If no errors return NULL
```
### Start payment 

#### Start payment with `PUBLIC_STORE_KEY`

Create ORDER_JSON with order information:
```java
{
   "amount": "100",
   "currency": "USD",
   "description": "Payment description", 
   "tracking_id" : "merchant_id", 
   "additional_data": {
      "contract": [ "recurring", "card_on_file" ] 
   }
}
```
Use your `PUBLIC_STORE_KEY` to start payment
```java
paymentModule.payWithPublicKey(PUBLIC_STORE_KEY, ORDER_JSON);
```

#### Start payment with `CHECKOUT_JSON`

Get `CHECKOUT_JSON` from YOUR_CHECKOUT_ENDPOINT/checkouts
Example:
```json
{
    "checkout": {
        "token": "623b342e6fa003ce273c6197380400137057cee5a4640822c2274f0fd3e278e2",
        "redirect_url": "https://checkout.begateway.com/checkout?token=623b342e6fa003ce273c6197380400137057cee5a4640822c2274f0fd3e278e2",
        "brands": [
            {
                "alternative": false,
                "name": "visa"
            },
            {
                "alternative": false,
                "name": "master"
            },
            {
                "alternative": false,
                "name": "belkart"
            },
            {
                "alternative": false,
                "name": "visa"
            },
            {
                "alternative": false,
                "name": "master"
            },
            {
                "alternative": false,
                "name": "maestro"
            }
        ],
        "company": {
            "name": "beGateway",
            "site": "https://begateway.com"
        },
        "description": "Payment description",
        "card_info": {}
    }
}
```
```java
paymentModule.payWithCheckoutJson(CHECKOUT_JSON);
```

#### Start payment with `CREDIT_CARD_JSON` with credit card token

Example
```json
{
   "request": {
      "token": "ccf6700bf372168e81fd3f4c2dd2e821524982a166ee83db7faa300e60a0f3e4", 
      "payment_method": "credit_card",
      "credit_card": {
          "number": "*",
          "token": "123e4567-e89b-12d3-a456-426655440000" 
      }
   }
}
```

```java
paymentModule.payWithCreditCard(CREDIT_CARD_JSON);
```
### Encryption
Use `encryptCardData` with your PUBLIC_STORE_KEY to get encrypted credit card data

For example:
```java 
String ENCRYPTED_NUMBER = paymentModule.encryptCardData("4200000000000000", PUBLIC_STORE_KEY);
String ENCRYPTED_CVV = paymentModule.encryptCardData("123", PUBLIC_STORE_KEY);
String ENCRYPTED_HOLDER = paymentModule.encryptCardData("IVAN IVANOV", PUBLIC_STORE_KEY);
String ENCRYPTED_EXPMONTH = paymentModule.encryptCardData("01", PUBLIC_STORE_KEY);
String ENCRYPTED_EXPYEAR = paymentModule.encryptCardData("2020", PUBLIC_STORE_KEY);
```

### Customization
You can customize card form view with `StyleSettings`

#### Create `StyleSettings`
```java
StyleSettings styleSettings = new StyleSettings();
```
StyleSettings parameters:
```java
* .setRequiredCardHolderName(false); // turn on/off card holder name field
* .setRequiredCardNumber(false); // turn on/off card number field
* .styleSettings.setMaskCVV(false); // turn on/off mask for CVV field
* .styleSettings.setMaskCardNumber(true); // turn on/off mask for card number field
* .setRequiredExpDate(false); // turn on/off expiration date field
* .setRequiredCVV(false); // turn on/off CVV field
* .setSaveCardCheckboxDefaultState(false); // set default state for save card toogle
* .setSaveCardCheckBoxVisible(false); // turn on/off visability of save card toogle
* .setSecuredLabelVisible(false); // turn on/off visability of secured label
* .setScanCardVisible(false); // turn on/off visability of scan card button
```

#### Set style settings to PaymentBuilder

```java
PaymentModule paymentModule = new PaymentBuilder()
    .setEndpoint(YOUR_CHECKOUT_ENDPOINT)
    .setPaymentResultListener(MainActivity.this)
    .setStyleSettings(styleSettings)
    .build(getApplicationContext(), MainActivity.this);
```
#### Cusomize using styles.xml
You can override styles for card form view elements in your styles.xml

```xml
<style name="begateway_generic_button" parent="@android:style/TextAppearance">
	<item name="android:textColor">@color/begateway_black</item>
</style>
```
```xml
<style name="begateway_form_fullscreen" parent="Theme.AppCompat.Light">
	<item name="windowNoTitle">true</item>
	<item name="android:background">@android:color/transparent</item>
	<item name="colorPrimary">@color/light_theme_primary</item>
	<item name="colorPrimaryDark">@color/light_theme_primary_dark</item>
	<item name="colorAccent">@color/light_theme_accent</item>
</style>
```
```xml
<style name="begateway_secured_label" parent="@android:style/TextAppearance">
	<item name="android:textSize">15dp</item>
	<item name="android:textColor">@color/begateway_black</item>
</style>
```
```xml
<style name="begateway_base_textview">
	<item name="android:textSize">@dimen/begateway_input_text_size</item>
	<item name="android:fontFamily">sans-serif-light</item>
</style>
```
```xml
<style name="begateway_text_input_layout">
	<item name="android:layout_width">match_parent</item>
	<item name="android:layout_height">wrap_content</item>
	<item name="android:layout_marginTop">12dp</item>
	<item name="android:accessibilityLiveRegion" tools:targetApi="kitkat">polite</item>
</style>
```
```xml
<style name="begateway_card_form_field" parent="begateway_base_textview">
	<item name="android:layout_width">match_parent</item>
	<item name="android:layout_height">60dp</item>
	<item name="android:paddingTop">@dimen/begateway_margin</item>
	<item name="android:textColorHint">@color/begateway_light_gray</item>
	<item name="android:textCursorDrawable" tools:ignore="NewApi">@null</item>
</style>
```

## See also
You can use gson library for json serialization/deserialization: https://github.com/google/gson 

## License

This repository is open source and available under the MIT license. For more information, see the LICENSE file.
