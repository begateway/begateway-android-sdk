# Фреймворк beGateway для Android
Компоненты beGateway для Android позволяют интегрировать прием платежей в ваши Android-приложения.

# Интеграция

## Установка

Добавьте репозиторий [![](https://jitpack.io/v/begateway/begateway-android-sdk.svg)](https://jitpack.io/#begateway/begateway-android-sdk) в ваш build файл и добавьте зависимость в Gradle.

### Подключение через Gradle

Добавьте репозиторий `jitpack` в конец списка репозиториев в файле `build.gradle` в корневом каталоге вашего проекта:

```groovy
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
```

Импортируйте SDK, указав зависимость в вашем файле `build.gradle`:

```groovy
   implementation 'com.github.begateway:begateway-android-sdk:2.5.0'
```
Вы можете посмотреть пример приложения на языке Kotlin с использованием SDK BeGateway для Android [здесь](https://github.com/begateway/begateway-android-sdk/tree/master/app).

## Использование

### Установка

Добавьте разрешения в манифест:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```
Инициализируйте модуль оплаты:

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
В `PaymentSdkBuilder` вы найдете полный список свойств.

### Реализация OnResultListener
```kotlin
class MainActivity : AppCompatActivity(), OnResultListener {

    //здесь вы получите токен платежа
    override fun onTokenReady(token: String) {
    }
    
    //здесь вы получите финальный статус платежа и токен платежной карты
    override fun onPaymentFinished(beGatewayResponse: BeGatewayResponse, cardToken: String?) {
    }
}
```
Параметры BeGatewayResponse:
```kotlin
    val status: ResponseStatus
    //SUCCESS("successful"),
    //ERROR("error"),
    //CANCELED("canceled"),
    //INCOMPLETE("incomplete"),
    //FAILED("failed"),
    //TIME_OUT("time_out");
    
    val message: String?     //детальная информация о статусе транзакции `ResponseStatus`
```
### Начало оплаты

#### Получение токена платежа:
```kotlin
            sdk.getPaymentToken(
                TokenCheckoutData(
                    Checkout(
                        test = BuildConfig.DEBUG,// true для работы в тестовом режиме
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
### Использование полученного токена:
#### 1. Для оплаты с вводом данных карты:
```kotlin
            startActivity(
                PaymentSdk.getCardFormIntent(this@MainActivity)
            )
```
#### 2. Для оплаты с помощью токена `CheckoutWithTokenData`:
Здесь можно также использовать другой токен, сохраненный на вашем сервере.

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
#### 3. Для оплаты с помощью сохраненной карты:
Для этого типа оплаты вам необходимо инициализировать SDK, получить токен платежа, а покупатель при оплате должен активировать переключатель "Save card". Токен карты будет сохранен, и при следующей оплате в интерфейсе появится возможность использовать для оплаты сохраненную карту:
```kotlin
   private fun payWithCard() {
        val token = binding.tilToken.editText?.text?.toString() ?: return
        startActivity( PaymentSdk.getSaveCardIntent(this@MainActivity, token))
    }
```
## Динамические поля 
К любому объекту из запроса можно добавить дополнительные поля с помощью методов класса `AdditionalFields`:
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
 Для управления динамическими полями используется сериализатор `CustomSerializer` и `AdditionalFields`.
## Шифрование
Используйте PaymentSdk.encryptData(data: String) для получения зашифрованных данных платежной карты.

Пример:
```kotlin
val encryptedCardNumber = sdk.encryptData("4200000000000000")
val encryptedHolder = sdk.encryptData("IVAN IVANOV")
val encryptedCvv = sdk.encryptData("123")
```

## Кастомизация
#### Кастомизация стилей
Вы можете адаптировать стили формы ввода данных карты, внеся изменения в файл `styles.xml`.

```xml
    <style name="begateway_Theme" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
        <!-- Основные цвета бренда. -->
        <item name="colorPrimary">@color/purple_200</item>
        <item name="colorPrimaryVariant">@color/purple_700</item>
        <item name="colorOnPrimary">@color/black</item>
        <!-- Дополнительные цвета бренда. -->
        <item name="colorSecondary">@color/teal_200</item>
        <item name="colorSecondaryVariant">@color/teal_200</item>
        <item name="colorOnSecondary">@color/black</item>
        <!-- Цвет строки состояния. -->
        <item name="android:statusBarColor" tools:targetApi="l">?attr/colorPrimaryVariant</item>
        <!-- Ниже можно добавить дополнительные настройки темы. -->
    </style>
```
#### Кастомизация текста
Вы можете изменить текст названий полей ввода карточных данных. Для это в файле `string.xml` замените значения соответствующих параметров:
```xml
<resources>
    <string name="begateway_form_hint_card_number">Card number</string>
    <string name="begateway_form_hint_expiration">Expiration date</string>
    <string name="begateway_form_hint_cardholder_name">Name on card</string>
</resources>
```

#### Кастомизация размера текста
Вы можете изменить значения размеров текста для полей ввода карточных данных. Для это в файле `dimens.xml` замените значения соответствующих параметров:
```xml
<resources>
    <dimen name="begateway_card_number_text_size">16sp</dimen>
    <dimen name="begateway_card_holder_text_size">16sp</dimen>
    <dimen name="begateway_card_expire_date_text_size">16sp</dimen>
    <dimen name="begateway_card_cvc_text_size">16sp</dimen>
</resources>
```

## Сканирование банковской карты 
### NFC
Если устройство поддерживает NFC, то в приложении будет отображаться кнопка для сканирования карты. 
### Сканирование с помощью камеры
Для сканирования карты с помощью камеры вы можете использовать любую библиотеку, добавив в манифест активность `ScanBankCardActivity`: 
```xml
        <activity android:name=".ScanBankCardActivity">
            <intent-filter>
                <action android:name="com.begateway.mobilepayments.action.SCAN_BANK_CARD"/>
            </intent-filter>
        </activity>
```
и реализуя логику выбранной библиотеки. В результате данные карты будут переданы в SDK begateway:
```kotlin
                setResult(
                      Activity.RESULT_OK,
                      PaymentSdk.getCardDataIntent(
                          scanResult.cardNumber,
                          scanResult.cardholderName,
                          scanResult.expiryMonth.toString(),//02
                          scanResult.expiryYear.toString(),//2027 или 27
                          scanResult.cvv                   // или используйте getCardDataIntentWithExpiryString() где expiryString может иметь значение 02/27 или 02/2027
                      )
                  )
                  finish()
```
## Google Pay

Если вы хотите принимать платежи с помощью Google Pay, добавьте следующий код в манифест:
```xml
        <meta-data
            android:name="com.google.android.gms.wallet.api.enabled"
            android:value="true" />
```
## Лицензия

beGateway SDK доступна под лицензией MIT. Подробнее в [файле LICENSE](https://github.com/begateway/begateway-android-sdk/blob/master/LICENSE).