package com.begateway.mobilepayments.models.settings

internal data class PaymentSdkSettings(
    var endpoint: String = "",
    var isDebugMode: Boolean = false,
    var publicKey: String = "",
    var returnUrl: String = "",
    var isWithEncryption: Boolean = false,
    var isCardNumberFieldVisible: Boolean = true,
    var isCardHolderFieldVisible: Boolean = true,
    var isCardDateFieldVisible: Boolean = true,
    var isCardCVCFieldVisible: Boolean = true,
    var isSaveCardVisible: Boolean = true,
)