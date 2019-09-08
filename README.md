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
   implementation 'com.github.begateway:begateway-android-sdk:1.0.0'
```

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

#### Start payment with `CREDIT_CARD_JSON`

Example
```json
{
   "request":{
      "token": "a349f2aac6d45f4b165c6da02a19ad3b93c9ad89392339c32210b3ec8fe9d3a3",
      "payment_method": "credit_card",
      "credit_card":{
         "number":"4200000000000000",
         "verification_value":"123",
         "holder":"IVAN IVANOV",
         "exp_month":"01",
         "exp_year":"2020"
      }
   }
}
```
or with token card
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
```java paymentModule.encryptCardData("4200000000000000", publicKey) ```
And then use it in `CREDIT_CARD_JSON`
```json
{
  "request": {
    "token": "",
    "payment_method": "credit_card",
    "encrypted_credit_card": {
      "number": "P5\/ydz1md1vGy15v2dSlno+qCw\/8HJ\/CDWgDWTDkdkMsIYLOIo4bI\/TOwa7qB5zkgfBypOk5OOC\/ZB0Rn+cJxLV5w3MC4j\/LAxxL3xwBiGekxiZF7xlMVSCBYdYPDt+9pWVMWDj34FP3SqJm6DiY5U973TN\/lQ77O8OIUP0eaGIa2pK20Bt0Uk\/yhdIki5MFd6rn10YCScOq88ZLeqIdDUw74KvppgNpZ7OYWyu8T050dCKc\/Z1rz9nRihLhm9SvVH8K+TKJEPhw8N2uXjFU5VSDEgSMTNWc8hpuL7x6ayW7VrjYk17FWtXC\/iXefWIVqEvgVAg8C4JKNkKdAK6b9A==",
      "verification_value": "hvgvuHoFZnvWhj763ESnqis8QaZBVhpx31X3ISepJB2h6kmWrkTVh2eL2z0tHY7S4J0ILubXGHCw8RRwhlhxh96nZMrJhkPa6Gb73xRg8lvlJE2aXWN+Szru81YnxYSaBhV28DeseCOXxYlqB\/\/f7jfsNcwll4HISFymKOeewz4u0fUzlLlOtSnk7SkUb8xJdLCtyDiMXAWoUsmr2vntTBLGiYgVrdbO\/RGvMKb58c591CFf9\/Pg5nnA9w11F0jSuASvBhzzt63AKDmlmPy4lXxXvC3lz\/BkhvOU1b3MNt2UMugpqfl2BgLUNMZGCuilYYB4sjSapUSOxVzTYZaAAQ==",
      "holder": "DjWromxDMQ0TPTjirxJll7nmaKFWt8fSLdPxJ0mF4fty3HGxQkzPp0Edr8AnpCOO3IW90pOtS7f3aiv+40pKOj0YHiQ\/oGtTl6y4yV342jKLMBqByWEhKgmOebIeyYKymJ3vsNu7do900UYT04EaCLjLjynt2eTu1htAC7+Vp8AlU1KaXS25TgVgOqPP7n4A6Q+JF6WCyfiyqs+5tbVgJMnnssJwT9MjnB6L+JD7LdOl6fMC4neYOMg5tjOWc5Al9gIbyS4+F1FZYTksLcFQQgJsLIHuby\/jKuzNuVWDBBbxriVoJt9XJXYWBkWG9LvunEPy0S\/IO4SllpsEBdNk3Q==",
      "exp_month": "f4P8J8PGs8BTSFndBSkmn+bEkr3ZIlRHy4fm+BLR9imF9TOLizCaTR+A6tX9HEgSfgAqIT3\/rfSgTHpsLgTCFwJ\/o\/C3jwEJBG6nZNYpFFp3v\/YHXjsZI8wIZUs0vcHVB+IJfgih9AGadFVy84G+RPxC627LrkvDzQLP\/y6d7CMNjscD5b7Rbp3E8HOetB27iGkgrmZ73cZekMXUavsZn7xInSXka8BjwBfIh3Gv7OCH03VpBW62yNRgs28MPQEkywVVFBRdNeTlm16QEjUMMSPASz\/rXEFildNyAGlheMeIm9cU07JBNfCCmXMFWuzWVbWCqYq9GnhsTeVHhiyLGw==",
      "exp_year": "FCUuvzHCg8VJxcMpj8TP+QXEuxKHkOEFPXqbiO0KFMvPRb4Q3uq3WmJ9OLTX1uyRDOErOBy96TkYRx9\/w1jCcvHoY+ktYtsp\/SPQvOYsICJTUiDiSGqXcHLCKYK84TQ1LM6WaXiF+UtYjmf8A3Sf3xb06mCC6r8W5LyV3lYa68JWHIsl2OMXKLTMP8XTlvAq7+a6w4NwbpbvU0HOr0jZju9HprvGdRu0h4Cp5mLfb07ultxi1JDMa4GY+qnq6vjGuKqh2iL+ewzpGd\/t7BdfXiaUZ1qe2\/sRvLxD386jyGRNg8An2IrLXr\/hB2yrsBHSI52tnsQePKiNDOIXrqhFrw=="
    }
  }
}
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
