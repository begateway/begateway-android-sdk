package com.begateway.mobilepayments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.begateway.mobilepayments.model.PaymentResultResponse;
import com.begateway.mobilepayments.utils.CardType;
import com.begateway.mobilepayments.view.CardEditText;
import com.begateway.mobilepayments.view.CardForm;

import java.util.Locale;

public class BaseCardFormActivity extends AppCompatActivity implements OnCardFormSubmitListener,
        CardEditText.OnCardTypeChangedListener {

    protected CardForm mCardForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PaymentModule paymentModule = PaymentModule.getInstance();
        PaymentSettings paymentSettings = paymentModule.getPaymentSettings();
        StyleSettings styleSettings = paymentSettings.getStyleSettings();

        Locale locale = new Locale(paymentSettings.getLocale());
        Locale.setDefault(locale);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.begateway_card_form);

        mCardForm = findViewById(R.id.card_form);
        mCardForm
                .cardRequired(styleSettings.isRequiredCardNumber())
                .maskCardNumber(styleSettings.isMaskCardNumber())
                .maskCvv(styleSettings.isMaskCVV())
                .expirationRequired(styleSettings.isRequiredExpDate())
                .cvvRequired(styleSettings.isRequiredCVV())
                .saveCardCheckBoxChecked(styleSettings.isSaveCardCheckboxDefaultState())
                .saveCardCheckBoxVisible(styleSettings.isSaveCardCheckBoxVisible())
                .cardHoldernameRequired(styleSettings.isRequiredCardHolderName())
                .actionLabel(styleSettings.getCustomPayButtonLabel() == null ? getString(R.string.begateway_button_pay) : styleSettings.getCustomPayButtonLabel())
                .scanCardVisible(styleSettings.isScanCardVisible())
                .securedLabelVisible(styleSettings.isSecuredLabelVisible())
                .setup(this);
        mCardForm.setOnCardFormSubmitListener(this);
        mCardForm.setOnCardTypeChangedListener(this);

//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    public void onCardTypeChanged(CardType cardType) {


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        PaymentModule.getInstance().onPaymentComplete(new PaymentResultResponse());
    }

    @Override
    public void onCardFormSubmit(PaymentResultResponse paymentResult) {

        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
    }

}
