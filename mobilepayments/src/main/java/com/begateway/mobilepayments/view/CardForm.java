package com.begateway.mobilepayments.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build.VERSION_CODES;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import androidx.appcompat.app.AppCompatActivity;

import com.begateway.mobilepayments.CardScanningFragment;
import com.begateway.mobilepayments.OnCardFormFieldFocusedListener;
import com.begateway.mobilepayments.OnCardFormSubmitListener;
import com.begateway.mobilepayments.OnCardFormValidListener;
import com.begateway.mobilepayments.PaymentModule;
import com.begateway.mobilepayments.PaymentSettings;
import com.begateway.mobilepayments.R;
import com.begateway.mobilepayments.model.PaymentResultResponse;
import com.begateway.mobilepayments.model.PaymentTokenResponse;
import com.begateway.mobilepayments.model.ResponseCode;
import com.begateway.mobilepayments.tasks.IPayWithCardTaskCallback;
import com.begateway.mobilepayments.tasks.IRetrievePaymentTokenTask;
import com.begateway.mobilepayments.utils.CardType;
import com.begateway.mobilepayments.view.CardEditText.OnCardTypeChangedListener;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class CardForm extends LinearLayout implements OnCardTypeChangedListener, OnFocusChangeListener, OnClickListener,
        OnEditorActionListener, TextWatcher {

    private List<ErrorEditText> mVisibleEditTexts;

    private CardEditText mCardNumber;
    private ExpirationDateEditText mExpiration;
    private CvvEditText mCvv;
    private CardholderNameEditText mCardholderName;
    private TextView securedLabel;

    private InitialValueCheckBox mSaveCardCheckBox;

    private Button mConfirmPayButton;
    private ImageButton mScanCardButton;
    private View mLoadingView;

    private boolean mCardNumberRequired;
    private boolean mExpirationRequired;
    private boolean mCvvRequired;
    private boolean mCardholderNameRequired;

    private String mActionLabel;
    private boolean mSaveCardCheckBoxVisible;
    private boolean mSaveCardCheckBoxChecked;

    private boolean mSecuredLabelVisible;

    private boolean mScanCardVisible;

    private boolean mValid = false;

    private Typeface customTypeface;

    private int customTextStyle;

    private OnCardFormValidListener mOnCardFormValidListener;
    private OnCardFormSubmitListener mOnCardFormSubmitListener;
    private OnCardFormFieldFocusedListener mOnCardFormFieldFocusedListener;
    private OnCardTypeChangedListener mOnCardTypeChangedListener;

    private CardScanningFragment mCardScanningFragment;

    public CardForm(Context context) {
        super(context);
        init();
    }

    public CardForm(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardForm(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public CardForm(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setVisibility(GONE);
        setOrientation(VERTICAL);

        inflate(getContext(), R.layout.begateway_card_form_fields, this);

        mCardNumber = findViewById(R.id.begateway_card_form_card_number);
        mExpiration = findViewById(R.id.begateway_card_form_expiration);
        mCvv = findViewById(R.id.begateway_card_form_cvv);
        mCardholderName = findViewById(R.id.begateway_card_form_cardholder_name);
        mSaveCardCheckBox = findViewById(R.id.begateway_card_form_save_card_checkbox);
        securedLabel = findViewById(R.id.begateway_secure_label);

        mVisibleEditTexts = new ArrayList<>();

        setListeners(mCardholderName);
        setListeners(mCardNumber);
        setListeners(mExpiration);
        setListeners(mCvv);

        mCardNumber.setOnCardTypeChangedListener(this);

        PaymentSettings paymentSettings = PaymentModule.getInstance().getPaymentSettings();
        if (paymentSettings.getPaymentTestData() != null){

            mCardNumber.setText(paymentSettings.getPaymentTestData().testCardNumber);
            mCardholderName.setText(paymentSettings.getPaymentTestData().testCardHolder);
            mCvv.setText(paymentSettings.getPaymentTestData().testCardCvv);
            mExpiration.setText(paymentSettings.getPaymentTestData().testExp);
        }

        mScanCardButton = findViewById(R.id.button_scan_card);

        mConfirmPayButton = findViewById(R.id.begateway_card_form_button_confirmpay);

    }

    public CardForm cardHoldernameRequired(boolean required) {
        mCardholderNameRequired = required;
        return this;
    }

    public CardForm cardRequired(boolean required) {
        mCardNumberRequired = required;
        return this;
    }

    public CardForm expirationRequired(boolean required) {
        mExpirationRequired = required;
        return this;
    }

    public CardForm cvvRequired(boolean required) {
        mCvvRequired = required;
        return this;
    }

    public CardForm actionLabel(String actionLabel) {
        mActionLabel = actionLabel;
        return this;
    }

    public CardForm maskCardNumber(boolean mask) {
        mCardNumber.setMask(mask);
        return this;
    }

    public CardForm maskCvv(boolean mask) {
        mCvv.setMask(mask);
        return this;
    }

    public CardForm saveCardCheckBoxVisible(boolean visible) {
        mSaveCardCheckBoxVisible = visible;
        return this;
    }

    public CardForm saveCardCheckBoxChecked(boolean checked) {
        mSaveCardCheckBoxChecked = checked;
        return this;
    }

    public CardForm securedLabelVisible(boolean visible){
        mSecuredLabelVisible = visible;
        return this;
    }

    public CardForm scanCardVisible(boolean visible){
        mScanCardVisible = visible;
        return this;
    }

    public void setup(final AppCompatActivity activity) {
        mCardScanningFragment = (CardScanningFragment)activity
                .getSupportFragmentManager()
                .findFragmentByTag(CardScanningFragment.TAG);

        if (mCardScanningFragment != null) {
            mCardScanningFragment.setCardForm(this);
        }

        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        mExpiration.useDialogForExpirationDateEntry(activity, true);

        setFieldVisibility(mCardholderName, mCardholderNameRequired);
        setFieldVisibility(mCardNumber, mCardNumberRequired);
        setFieldVisibility(mExpiration, mExpirationRequired);
        setFieldVisibility(mCvv, mCvvRequired);
        setViewVisibility(mSaveCardCheckBox, mSaveCardCheckBoxVisible);

        setViewVisibility(securedLabel, mSecuredLabelVisible);
        setViewVisibility(mScanCardButton, mScanCardVisible);

        securedLabel.setText(getContext().getString(R.string.begateway_secure_info, PaymentModule.getInstance().getPaymentSettings().getSecuredBy()));

        TextInputEditText editText;
        for (int i = 0; i < mVisibleEditTexts.size(); i++) {
            editText = mVisibleEditTexts.get(i);
            if (i == mVisibleEditTexts.size() - 1) {
                editText.setImeOptions(EditorInfo.IME_ACTION_GO);
                editText.setImeActionLabel(mActionLabel, EditorInfo.IME_ACTION_GO);
                editText.setOnEditorActionListener(this);

            } else {
                editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                editText.setImeActionLabel(null, EditorInfo.IME_ACTION_NONE);
                editText.setOnEditorActionListener(null);

            }
        }

        mSaveCardCheckBox.setInitiallyChecked(mSaveCardCheckBoxChecked);

        mScanCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCard(activity);
            }
        });

        RelativeLayout loadingView = new RelativeLayout(getContext());
        loadingView.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        loadingView.setBackgroundColor(getResources().getColor(R.color.begateway_loading));

        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        ProgressBar progressBar = new ProgressBar(getContext());
        progressBar.setLayoutParams(layoutParams);

        loadingView.addView(progressBar);
        loadingView.setGravity(Gravity.CENTER);
        loadingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        activity.addContentView(loadingView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        mLoadingView = loadingView;
        mLoadingView.setVisibility(INVISIBLE);

        mConfirmPayButton.setText(mActionLabel);

        mConfirmPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValid()) {

                    String cardNumber = getCardNumber();
                    String cardCvv = getCvv();
                    String cardHolder = getCardholderName();
                    String cardExpMonth = getExpirationMonth();
                    String cardExpYear = getExpirationYear();

                    mLoadingView.setVisibility(VISIBLE);

                    activity.setFinishOnTouchOutside(false);

                    PaymentModule.getInstance().payWithCardInternal(
                            activity,
                            cardNumber,
                            cardCvv,
                            cardHolder,
                            cardExpMonth,
                            cardExpYear,
                            new IPayWithCardTaskCallback() {
                                @Override
                                public void onCallback(PaymentResultResponse response) {

                                    mLoadingView.setVisibility(INVISIBLE);
                                    activity.setFinishOnTouchOutside(true);

                                    if (response != null && mSaveCardCheckBoxVisible) {
                                        response.setSaveCard(isSaveCardCheckBoxChecked());
                                    }
                                    if (mOnCardFormSubmitListener != null) {
                                        mOnCardFormSubmitListener.onCardFormSubmit(response);
                                    }
                                }
                            }
                    );
                }
                else {
                    validate();
                }

            }
        });


        setVisibility(VISIBLE);

       if (PaymentModule.getInstance().isNeedToGetPaymentToken()){

           mLoadingView.setVisibility(VISIBLE);

           PaymentModule.getInstance().getPaymentToken(new IRetrievePaymentTokenTask() {
               @Override
               public void onCallback(final PaymentTokenResponse paymentToken) {

                   mLoadingView.setVisibility(INVISIBLE);

                   if (paymentToken.getStatus() != ResponseCode.SUCCESS){
                       if (mOnCardFormSubmitListener != null) {

                           PaymentResultResponse paymentResultResponse = new PaymentResultResponse();
                           paymentResultResponse.setError(paymentToken.getError());
                           mOnCardFormSubmitListener.onCardFormSubmit(paymentResultResponse);
                       }
                   }
               }
           });
       }

    }

    public boolean isCardScanningAvailable() {
        try {
            return CardIOActivity.canReadCardWithCamera();
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public void scanCard(AppCompatActivity activity) {
        if (isCardScanningAvailable() && mCardScanningFragment == null) {
            mCardScanningFragment = CardScanningFragment.requestScan(activity, this);
        }
    }

    /**
     * Use {@link #handleCardIOResponse(int, Intent)} instead.
     */
    @SuppressLint("DefaultLocale")
    @Deprecated
    public void handleCardIOResponse(Intent data) {
        handleCardIOResponse(Integer.MIN_VALUE, data);
    }

    @SuppressLint("DefaultLocale")
    public void handleCardIOResponse(int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_CANCELED || resultCode == Activity.RESULT_OK) {
            mCardScanningFragment = null;
        }

        if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

            mCardholderName.setText(scanResult.cardholderName);
            mCardholderName.focusNextView();

            if (mCardNumberRequired) {
                mCardNumber.setText(scanResult.cardNumber);
                mCardNumber.focusNextView();
            }

            if (scanResult.isExpiryValid() && mExpirationRequired) {
                mExpiration.setText(String.format("%02d%d", scanResult.expiryMonth, scanResult.expiryYear));
                mExpiration.focusNextView();
            }
        }
    }

    private void setListeners(EditText editText) {
        editText.setOnFocusChangeListener(this);
        editText.setOnClickListener(this);
        editText.addTextChangedListener(this);
    }

    private void setViewVisibility(View view, boolean visible) {
        view.setVisibility(visible ? VISIBLE : GONE);
    }

    private void setFieldVisibility(ErrorEditText editText, boolean visible) {

        setViewVisibility(editText, visible);
        if (editText.getTextInputLayoutParent() != null) {
            setViewVisibility(editText.getTextInputLayoutParent(), visible);
        }

        if (visible) {
            mVisibleEditTexts.add(editText);
        } else {
            mVisibleEditTexts.remove(editText);
        }
    }

    public void setOnCardFormValidListener(OnCardFormValidListener listener) {
        mOnCardFormValidListener = listener;
    }

    public void setOnCardFormSubmitListener(OnCardFormSubmitListener listener) {
        mOnCardFormSubmitListener = listener;
    }

    public void setOnFormFieldFocusedListener(OnCardFormFieldFocusedListener listener) {
        mOnCardFormFieldFocusedListener = listener;
    }

    public void setOnCardTypeChangedListener(OnCardTypeChangedListener listener) {
        mOnCardTypeChangedListener = listener;
    }

    public void setEnabled(boolean enabled) {
        mCardholderName.setEnabled(enabled);
        mCardNumber.setEnabled(enabled);
        mExpiration.setEnabled(enabled);
        mCvv.setEnabled(enabled);
    }

    public boolean isValid() {

        boolean valid = true;
        if (mCardholderNameRequired) {
            valid = valid && mCardholderName.isValid();
        }
        if (mCardNumberRequired) {
            valid = valid && mCardNumber.isValid();
        }
        if (mExpirationRequired) {
            valid = valid && mExpiration.isValid();
        }
        if (mCvvRequired) {
            valid = valid && mCvv.isValid();
        }
        return valid;
    }

    public void validate() {

        if (mCardholderNameRequired) {
            mCardholderName.validate();
        }
        if (mCardNumberRequired) {
            mCardNumber.validate();
        }
        if (mExpirationRequired) {
            mExpiration.validate();
        }
        if (mCvvRequired) {
            mCvv.validate();
        }
    }

    public CardholderNameEditText getCardholderNameEditText() {
        return mCardholderName;
    }

    public CardEditText getCardEditText() {
        return mCardNumber;
    }

    public ExpirationDateEditText getExpirationDateEditText() {
        return mExpiration;
    }

    public CvvEditText getCvvEditText() {
        return mCvv;
    }

    public void setCardholderNameError(String errorMessage) {
        if (mCardholderNameRequired) {
            mCardholderName.setError(errorMessage);
            if (!mCardNumber.isFocused() && !mExpiration.isFocused() && !mCvv.isFocused()) {
                requestEditTextFocus(mCardholderName);
            }
        }
    }

    public void setCardNumberError(String errorMessage) {
        if (mCardNumberRequired) {
            mCardNumber.setError(errorMessage);
            requestEditTextFocus(mCardNumber);
        }
    }

    public void setExpirationError(String errorMessage) {
        if (mExpirationRequired) {
            mExpiration.setError(errorMessage);
            if (!mCardNumber.isFocused()) {
                requestEditTextFocus(mExpiration);
            }
        }
    }

    public void setCvvError(String errorMessage) {
        if (mCvvRequired) {
            mCvv.setError(errorMessage);
            if (!mCardNumber.isFocused() && !mExpiration.isFocused()) {
                requestEditTextFocus(mCvv);
            }
        }
    }

    private void requestEditTextFocus(EditText editText) {
        editText.requestFocus();
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public void closeSoftKeyboard() {
        mCardNumber.closeSoftKeyboard();
    }

    public String getCardholderName() {
        return mCardholderName.getText().toString();
    }

    public String getCardNumber() {
        return mCardNumber.getText().toString();
    }

    public String getExpirationMonth() {
        return mExpiration.getMonth();
    }

    public String getExpirationYear() {
        return mExpiration.getYear();
    }

    public String getCvv() {
        return mCvv.getText().toString();
    }

    public boolean isSaveCardCheckBoxChecked() {
        return mSaveCardCheckBox.isChecked();
    }


    @Override
    public void onCardTypeChanged(CardType cardType) {
        mCvv.setCardType(cardType);

        if (mOnCardTypeChangedListener != null) {
            mOnCardTypeChangedListener.onCardTypeChanged(cardType);
        }

        boolean isSupported = PaymentModule.getInstance().isCardSupported(cardType);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus && mOnCardFormFieldFocusedListener != null) {
            mOnCardFormFieldFocusedListener.onCardFormFieldFocused(v);
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnCardFormFieldFocusedListener != null) {
            mOnCardFormFieldFocusedListener.onCardFormFieldFocused(v);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        boolean valid = isValid();
        if (mValid != valid) {
            mValid = valid;
            if (mOnCardFormValidListener != null) {
                mOnCardFormValidListener.onCardFormValid(valid);
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO && mOnCardFormSubmitListener != null) {
            mConfirmPayButton.requestFocus();
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(v.getWindowToken(), 0);
            validate();
            return true;
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
