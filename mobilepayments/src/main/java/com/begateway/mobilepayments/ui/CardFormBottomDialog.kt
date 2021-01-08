package com.begateway.mobilepayments.ui

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager.LayoutParams.*
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.databinding.BegatewayFragmentCardFormBinding
import com.begateway.mobilepayments.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


private const val MIN_LENGTH_EXPIRY = 7
private const val REQUEST_CODE_SCAN_BANK_CARD = 0x56BD
private const val BANK_CARD_REGEX = "[^\\d ]*"

internal class CardFormBottomDialog : BottomSheetDialogFragment() {
    private val minExpiry = Calendar.getInstance()
    private var currentCardType: CardType = CardType.EMPTY
    private val intent = Intent("com.begateway.mobilepayments.action.SCAN_BANK_CARD")
    private val expiryFormat = SimpleDateFormat("MM/yyyy", Locale.US)
    private var binding: BegatewayFragmentCardFormBinding? = null
    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var activityInfo: ActivityInfo? = null
    private val payButtonState: (text: Editable?) -> Unit = { _ ->
        binding?.mbPay?.isEnabled = isAllFieldCorrect()
    }
    private val inputFilterList: Array<InputFilter> = arrayOf(
        inputFilter(BANK_CARD_REGEX),
        InputFilter.LengthFilter(currentCardType.maxCardLength)
    )

    init {
        val year = minExpiry.get(Calendar.YEAR)
        val month = minExpiry.get(Calendar.MONTH)
        minExpiry.clear()
        minExpiry.set(year, month, 1)
        minExpiry.add(Calendar.DAY_OF_MONTH, -1)
        setStyle(STYLE_NO_FRAME, R.style.begateway_MainDialogTheme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.window?.addFlags(FLAG_SECURE)
    }

    override fun onDetach() {
        activity?.window?.clearFlags(FLAG_SECURE)
        super.onDetach()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
    }

    override fun onDismiss(dialog: DialogInterface) {
        view?.hideSoftKeyboard()
        super.onDismiss(dialog)
        activity?.finish()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = BegatewayFragmentCardFormBinding.inflate(
        inflater,
        container,
        false
    ).also {
        binding = it
    }.root

    private fun isAllFieldCorrect(): Boolean {
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityInfo = requireContext().findDefaultLocalActivityForIntent(intent)
        if (Build.VERSION.SDK_INT >= 30) {//чекнуть надо ли это??? и в связке с setDecorFitsSystemWindows должен быть листенер
            dialog?.window?.setDecorFitsSystemWindows(false)
            dialog?.window?.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        } else {
            dialog?.window?.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE or SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            (dialog as? BottomSheetDialog)?.also { dialog ->
                val bottomSheet =
                    dialog.findViewById<FrameLayout?>(com.google.android.material.R.id.design_bottom_sheet)!!
                BottomSheetBehavior.from(bottomSheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isHideable = true
                    peekHeight = 0
                }
            }
        }
        view.viewTreeObserver?.addOnGlobalLayoutListener(onGlobalLayoutListener)

        binding?.run {
            mbPay.isEnabled = isAllFieldCorrect()
            toolbar.run {
                (activity as CheckoutActivity?)?.let {
                    it.setToolBar(toolbar, R.color.begateway_primary_black) {
                        dismissAllowingStateLoss()
                    }
                }
            }
            tilCardName.editText?.run {
                addTextChangedListener(afterTextChanged = payButtonState)
            }
            tilCardDate.editText?.run {
                addTextChangedListener(afterTextChanged = payButtonState)
            }
            tilCardCvv.editText?.run {
                addTextChangedListener(afterTextChanged = payButtonState)
            }
        }
        initCardNumberView()
    }

    private fun initCardNumberView() {
        binding?.run {
            tilCardNumber.setStartIconTintList(null)
            tilCardNumber.editText?.run {
                setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        isCardNumberCorrect()
                    }
                }
                addTextChangedListener(
                    onTextChanged = { charSequence: CharSequence?, _: Int, _: Int, _: Int ->
                        val pan = charSequence?.toString() ?: ""
                        val newCardType = CardType.getCardTypeByPan(pan.filter(Char::isDigit))
                        if (currentCardType != newCardType) {
                            currentCardType = newCardType
                            cardTypeUpdated()
                        }
                        setEndCardNumberMode(pan)
                    },
                    afterTextChanged = payButtonState
                )
                setEndCardNumberMode()
                cardTypeUpdated()
                configureForCardNumberInput()
            }

        }
    }

    private fun cardTypeUpdated() {
        binding?.tilCardNumber?.run {
            editText?.let {
                inputFilterList[1] = InputFilter.LengthFilter(currentCardType.maxCardLength)
                it.filters = inputFilterList
            }
            startIconDrawable =
                AppCompatResources.getDrawable(
                    requireContext(),
                    currentCardType.drawable
                )
        }
    }

    private fun setEndCardNumberMode(cardNumber: String? = null) {
        binding?.run {
            if (activityInfo != null && cardNumber.isNullOrEmpty()) {
                tilCardNumber.endIconMode = TextInputLayout.END_ICON_CUSTOM
                tilCardNumber.endIconDrawable =
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.begateway_ic_scan_card_data
                    )
                tilCardNumber.setEndIconOnClickListener {
                    intent.component =
                        ComponentName(activityInfo!!.packageName, activityInfo!!.name)
                    startActivityForResult(intent, REQUEST_CODE_SCAN_BANK_CARD)
                }
            } else {
                if (TextInputLayout.END_ICON_CLEAR_TEXT != tilCardNumber.endIconMode) {
                    tilCardNumber.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }
            }
        }
    }

    private fun isCardNumberCorrect(): Boolean =
        binding?.tilCardNumber?.editText
            ?.text?.toString()
            ?.isCorrectPan(currentCardType.minCardLength..currentCardType.maxCardLength) ?: false

    private fun isExpiryCorrect(): Boolean {
        val editText = binding?.tilCardDate?.editText
        return editText?.length() == MIN_LENGTH_EXPIRY &&
                try {
                    val text = editText.text?.toString()
                    !text.isNullOrEmpty() && minExpiry.time < expiryFormat.parse(text)
                } catch (e: ParseException) {
                    false
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        binding = null
    }
}