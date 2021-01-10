package com.begateway.mobilepayments.ui

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.databinding.BegatewayFragmentCardFormBinding
import com.begateway.mobilepayments.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

private const val EXPIRY_DATE_LENGTH = 7
private const val MIN_LENGTH_NAME = 3
private const val REQUEST_CODE_SCAN_BANK_CARD = 0x56BD
private const val BANK_CARD_REGEX = "[^\\d ]*"
private const val MAXIMUM_VALID_YEAR_DIFFERENCE = 21

internal class CardFormBottomDialog : BottomSheetDialogFragment() {
    private val minExpiry = Calendar.getInstance()
    private var currentCardType: CardType = CardType.EMPTY
    private val intent = Intent("com.begateway.mobilepayments.action.SCAN_BANK_CARD")
    private val expiryFormat = SimpleDateFormat("MM/yyyy", Locale.US)
    private var binding: BegatewayFragmentCardFormBinding? = null
    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var activityInfo: ActivityInfo? = null

    private val cardInputFilterList: Array<InputFilter> = arrayOf(
        inputFilterDigits(BANK_CARD_REGEX),
        InputFilter.LengthFilter(currentCardType.getMaxCardLength())
    )
    private val cvcInputFilterList: Array<InputFilter> = arrayOf(
        inputFilterDigits(BANK_CARD_REGEX),
        InputFilter.LengthFilter(currentCardType.getMaxCVCLength())
    )
    private val inputTypeMask = maskFormatWatcher(currentCardType.maskFormat)

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
        activityInfo = requireContext().findDefaultLocalActivityForIntent(intent)
        setHasOptionsMenu(true)
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        if (Build.VERSION.SDK_INT >= 30) {//чекнуть надо ли это??? и в связке с setDecorFitsSystemWindows должен быть еще листенер
//            dialog?.window?.setDecorFitsSystemWindows(false)
//        } else {
//            dialog?.window?.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)
//        }
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
            updateButtonState()
            toolbar.run {
                (activity as CheckoutActivity?)?.let {
                    it.setToolBar(toolbar, R.color.begateway_primary_black) {
                        dismissAllowingStateLoss()
                    }
                }
            }
            mcbSaveCard
        }
        initCardNumberView()
        initCardNameView()
        initCardExpireDateView()
        initCvcView()
        applyCardTypeValues()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.begateway_scan_card_menu, menu)
        menu.findItem(R.id.action_scan_camera).isVisible = activityInfo != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_scan_camera -> {
                intent.component = ComponentName(activityInfo!!.packageName, activityInfo!!.name)
                startActivityForResult(intent, REQUEST_CODE_SCAN_BANK_CARD)
                true
            }
            R.id.action_scan_nfc -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateButtonState() {
        binding?.mbPay?.isEnabled = isAllFieldCorrect()
    }

    private fun initCardNumberView() {
        binding?.run {
            tilCardNumber.apply {
                requestFocus()
                setStartIconTintList(null)
                onFocusListener(
                    ::isCardNumberCorrect
                ) { getString(R.string.begateway_card_number_invalid) }
                onTextChanged(
                    ::updateButtonState,
                    ::requestFocusToNextVisibleElement,
                    ::isCardMaxLengthAccepted
                )
                onCardNumberWatcher(
                    ::setCardType
                )
            }

            tietCardNumber.run {
                onEditorListener(::requestFocusToNextVisibleElement)
                installMask(inputTypeMask)
            }
        }
    }

    private fun isCardMaxLengthAccepted(length: Int) = currentCardType.getMaxCardLength() == length

    private fun initCardNameView() {
        binding?.run {
            tilCardName.apply {
                onFocusListener(
                    ::isCardNameCorrect
                ) { getString(R.string.begateway_cardholder_name_required) }
                onTextChanged(
                    ::updateButtonState,
                    ::requestFocusToNextVisibleElement,
                ) {
                    false
                }
            }

            tietCardName.run {
                onEditorListener(::requestFocusToNextVisibleElement)
            }
        }
    }

    private fun initCardExpireDateView() {
        binding?.run {
            tilCardExpiryDate.apply {
                onFocusListener(
                    ::isExpiryCorrect
                ) { getString(R.string.begateway_expiration_invalid) }
                onTextChanged(
                    ::updateButtonState,
                    ::requestFocusToNextVisibleElement,
                    ::isCardExpireLengthAccepted
                )
                onExpiryTextChanged()
            }
            tietCardExpiryDate.run {
                onEditorListener(::requestFocusToNextVisibleElement)
                val firstTwoNumber = minExpiry.get(Calendar.YEAR) / 100
                val firstTwoNumberDifference =
                    (minExpiry.get(Calendar.YEAR) + MAXIMUM_VALID_YEAR_DIFFERENCE) / 100
                installMask(
                    maskFormatWatcher(
                        "__/${
                            if (firstTwoNumber == firstTwoNumberDifference) {
                                firstTwoNumber
                            } else {
                                firstTwoNumber / 10
                            }
                        }__"
                    )
                )
            }
        }
    }

    private fun isCardExpireLengthAccepted(length: Int) = EXPIRY_DATE_LENGTH == length

    private fun initCvcView() {
        binding?.run {
            tilCardCvc.apply {
                onFocusListener(
                    ::isCVCCorrect,
                    ::getCvcError
                )
                onTextChanged(
                    ::updateButtonState,
                    ::requestFocusToNextVisibleElement,
                    ::isCardCvcLengthAccepted
                )
            }

            tietCvc.onEditorListener(::requestFocusToNextVisibleElement)
        }
    }

    private fun isCardCvcLengthAccepted(length: Int) = currentCardType.getMaxCVCLength() == length

    private fun getCvcError() = String.format(
        getString(R.string.begateway_cvv_invalid),
        getString(currentCardType.securityCodeName)
    )

    private fun setCardType(cardType: CardType) {
        if (currentCardType != cardType) {
            currentCardType = cardType
            applyCardTypeValues()
        }
    }

    private fun applyCardTypeValues() {
        binding?.run {
            tilCardNumber.apply {
                editText?.let {
                    cardInputFilterList[1] =
                        InputFilter.LengthFilter(currentCardType.getMaxCardLength())
                    it.filters = cardInputFilterList
                    inputTypeMask.changeMask(
                        getMaskDescriptor(currentCardType.maskFormat).setInitialValue(
                            it.text.toString()
                        )
                    )
                }
                startIconDrawable =
                    AppCompatResources.getDrawable(
                        requireContext(),
                        currentCardType.drawable
                    )
            }
            tilCardCvc.apply {
                if (isVisible) {
                    hint = getString(currentCardType.securityCodeName)
                    editText?.let {
                        cvcInputFilterList[1] =
                            InputFilter.LengthFilter(currentCardType.getMaxCVCLength())
                        it.filters = cvcInputFilterList
                        if (!it.text.isNullOrEmpty()) {
                            error = if (isCVCCorrect()) {
                                null
                            } else {
                                getCvcError()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestFocusToNextVisibleElement(currentElement: EditText): Boolean {
        binding?.run {
            val listOfViews = arrayListOf(
                tilCardNumber,
                tilCardName,
                tilCardExpiryDate,
                tilCardCvc
            )
                .filter { it.isVisible }
                .map { it.editText }
            val listSize = listOfViews.size - 1
            listOfViews.forEachIndexed { index, view ->
                if (view == currentElement && listSize > index) {
                    view.clearFocus()
                    listOfViews[index + 1]?.requestFocus()
                    return true
                } else if (listSize == index) {
                    view?.clearFocus()
                    view?.hideSoftKeyboard()
                    return false
                }
            }
        }
        return false
    }

    private fun isAllFieldCorrect(): Boolean {
        return isCardNumberCorrect() && isCardNameCorrect() && isExpiryCorrect() && isCVCCorrect()
    }

    private fun isCardNameCorrect(): Boolean =
        if (binding?.tilCardName?.isVisible == true) {
            (binding?.tilCardName?.editText?.text?.trim()?.length ?: 0) >= MIN_LENGTH_NAME
        } else {
            true
        }

    private fun isCVCCorrect(): Boolean =
        if (binding?.tilCardCvc?.isVisible == true) {
            currentCardType.listOfSecurityCodeSizes.contains(
                binding?.tilCardCvc?.editText?.text?.length ?: 0
            )
        } else {
            true
        }

    private fun isCardNumberCorrect(): Boolean =
        binding?.tilCardNumber?.editText
            ?.text?.toString()
            ?.isCorrectPan(
                currentCardType.listOfCardNumberSizes,
                currentCardType.isLunhCheckRequired
            )
            ?: false

    private fun isExpiryCorrect(): Boolean =
        if (binding?.tilCardExpiryDate?.isVisible == true) {
            val editText = binding?.tilCardExpiryDate?.editText
            editText?.length() == EXPIRY_DATE_LENGTH &&
                    try {
                        val text = editText.text?.toString()
                        !text.isNullOrEmpty() && minExpiry.time < expiryFormat.parse(text)
                    } catch (e: ParseException) {
                        false
                    }
        } else {
            true
        }

}