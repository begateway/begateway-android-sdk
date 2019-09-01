package com.begateway.mobilepayments.view;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.begateway.mobilepayments.R;
import com.begateway.mobilepayments.utils.CardType;


public class CvvEditText extends ErrorEditText implements TextWatcher {

    private static final int DEFAULT_MAX_LENGTH = 3;

    private CardType mCardType;

    public CvvEditText(Context context) {
        super(context);
        init();
    }

    public CvvEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CvvEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setInputType(InputType.TYPE_CLASS_NUMBER);
        setFilters(new InputFilter[]{new LengthFilter(DEFAULT_MAX_LENGTH)});
        addTextChangedListener(this);
    }

    public void setCardType(CardType cardType) {
        mCardType = cardType;

        InputFilter[] filters = { new LengthFilter(cardType.getSecurityCodeLength()) };
        setFilters(filters);

        setContentDescription(getContext().getString(cardType.getSecurityCodeName()));
        setFieldHint(cardType.getSecurityCodeName());

        invalidate();
    }

    public void setMask(boolean mask) {
        if (mask) {
            setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        } else {
            setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (mCardType == null) {
            return;
        }

        if (mCardType.getSecurityCodeLength() == editable.length() && getSelectionStart() == editable.length()) {
            validate();

            if (isValid()) {
                focusNextView();
            }
        }
    }

    @Override
    public boolean isValid() {
        return isOptional() || getText().toString().length() == getSecurityCodeLength();
    }

    @Override
    public String getErrorMessage() {
        String securityCodeName;
        if (mCardType == null) {
            securityCodeName = getContext().getString(R.string.begateway_cvv);
        } else {
            securityCodeName = getContext().getString(mCardType.getSecurityCodeName());
        }

        if (TextUtils.isEmpty(getText())) {
            return getContext().getString(R.string.begateway_cvv_required, securityCodeName);
        } else {
            return getContext().getString(R.string.begateway_cvv_invalid, securityCodeName);
        }
    }

    private int getSecurityCodeLength() {
        if (mCardType == null) {
            return DEFAULT_MAX_LENGTH;
        } else {
            return mCardType.getSecurityCodeLength();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
}
