package com.begateway.mobilepayments.view;

import android.content.Context;
import android.graphics.Rect;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.begateway.mobilepayments.R;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

public class ErrorEditText extends TextInputEditText {

    private Animation mErrorAnimator;
    private boolean mError;
    private boolean mOptional;

    public ErrorEditText(Context context) {
        super(context);
        init();
    }

    public ErrorEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ErrorEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mErrorAnimator = AnimationUtils.loadAnimation(getContext(), R.anim.begateway_error_animation);
        mError = false;
        setupRTL();
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (lengthBefore != lengthAfter) {
            setError(null);
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if(!focused && !isValid() && !TextUtils.isEmpty(getText())) {
            setError(getErrorMessage());
        }
    }

    public void setFieldHint(int hint) {
        setFieldHint(getContext().getString(hint));
    }

    public void setFieldHint(String hint) {
        if (getTextInputLayoutParent() != null) {
            getTextInputLayoutParent().setHint(hint);
        } else {
            setHint(hint);
        }
    }

    @SuppressWarnings("WrongConstant")
    public View focusNextView() {
        if (getImeActionId() == EditorInfo.IME_ACTION_GO) {
            return null;
        }

        View next;
        try {
            next = focusSearch(View.FOCUS_FORWARD);
        } catch (IllegalArgumentException e) {
            // View.FOCUS_FORWARD results in a crash in some versions of Android
            // https://github.com/braintree/braintree_android/issues/20
            next = focusSearch(View.FOCUS_DOWN);
        }
        if (next != null && next.requestFocus()) {
            return next;
        }

        return null;
    }

    public void setOptional(boolean optional) {
        mOptional = optional;
    }

    public boolean isOptional() {
        return mOptional;
    }

    public boolean isError() {
        return mError;
    }

    public void setError(@Nullable String errorMessage) {
        mError = !TextUtils.isEmpty(errorMessage);

        TextInputLayout textInputLayout = getTextInputLayoutParent();
        if (textInputLayout != null) {
            textInputLayout.setErrorEnabled(!TextUtils.isEmpty(errorMessage));
            textInputLayout.setError(errorMessage);
        }

        if (mErrorAnimator != null && mError) {
            startAnimation(mErrorAnimator);
        }
    }

    public boolean isValid() {
        return true;
    }

    public String getErrorMessage() {
        return null;
    }

    public void validate() {
        if (isValid() || isOptional()) {
            setError(null);
        } else {
            setError(getErrorMessage());
        }
    }


    public void closeSoftKeyboard() {
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(getWindowToken(), 0);
    }


    @Nullable
    public TextInputLayout getTextInputLayoutParent() {
        if (getParent() != null && getParent().getParent() instanceof TextInputLayout) {
            return (TextInputLayout) getParent().getParent();
        }

        return null;
    }

    private void setupRTL() {
        if (SDK_INT >= JELLY_BEAN_MR1) {
            if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                setTextDirection(View.TEXT_DIRECTION_LTR);
                setGravity(Gravity.END);
            }
        }
    }
}
