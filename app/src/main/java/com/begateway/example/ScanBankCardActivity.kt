package com.begateway.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.begateway.mobilepayments.PaymentSdk
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard

class ScanBankCardActivity : AppCompatActivity() {

    val REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scanIntent = Intent(this, CardIOActivity::class.java)
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true)
        startActivityForResult(scanIntent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                (data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT) as CreditCard?)?.let { scanResult ->
                    setResult(
                        Activity.RESULT_OK,
                        PaymentSdk.getCardDataIntent(
                            scanResult.cardNumber,
                            scanResult.cardholderName,
                            scanResult.expiryMonth.toString(),//02
                            scanResult.expiryYear.toString(),//2021 or 21 or you can use getCardDataIntentWithExpiryString() where expiryString can be 02/21 or 02/2021
                            scanResult.cvv
                        )
                    )
                } ?: setResult(Activity.RESULT_CANCELED)
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
            finish()
        }
    }

}