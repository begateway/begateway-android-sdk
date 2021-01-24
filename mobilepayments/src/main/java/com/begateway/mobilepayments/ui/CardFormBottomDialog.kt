package com.begateway.mobilepayments.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.nfc.NfcAdapter
import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.begateway.mobilepayments.PaymentSdk
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.cardScanner.ui.NfcScannerActivity
import com.begateway.mobilepayments.databinding.BegatewayFragmentCardFormBinding
import com.begateway.mobilepayments.model.CardData
import com.begateway.mobilepayments.model.CreditCard
import com.begateway.mobilepayments.model.PaymentMethodType
import com.begateway.mobilepayments.model.Request
import com.begateway.mobilepayments.model.network.request.PaymentRequest
import com.begateway.mobilepayments.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.tinkoff.decoro.watchers.DescriptorFormatWatcher
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

private const val EXPIRY_DATE_LENGTH = 7
private const val MIN_LENGTH_NAME = 3
private const val REQUEST_CODE_SCAN_BANK_CARD = 0x56BD
private const val BANK_CARD_REGEX = "[^\\d ]*"
private const val MAXIMUM_VALID_YEAR_DIFFERENCE = 30

internal class CardFormBottomDialog : BottomSheetDialogFragment() {

    private val minExpiry = Calendar.getInstance()
    private var currentCardType: CardType = CardType.EMPTY
    private val intent = Intent("com.begateway.mobilepayments.action.SCAN_BANK_CARD")
    private val expiryFormat =
        SimpleDateFormat(CardData.EXPIRY_DATE_FORMAT_FULL, Locale.US)
    private var binding: BegatewayFragmentCardFormBinding? = null
    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var activityInfo: ActivityInfo? = null
    private var nfcAdapter: NfcAdapter? = null
    private var onProgressDialogListener: OnProgressDialogListener? = null

    private val cardInputFilterList: Array<InputFilter> = arrayOf(
        inputFilterDigits(BANK_CARD_REGEX),
        InputFilter.LengthFilter(currentCardType.getMaxCardLength())
    )
    private val cvcInputFilterList: Array<InputFilter> = arrayOf(
        inputFilterDigits(BANK_CARD_REGEX),
        InputFilter.LengthFilter(currentCardType.getMaxCVCLength())
    )
    private var cardNumberInputTypeMask: DescriptorFormatWatcher? = null
    private var expiryDateTypeMask: DescriptorFormatWatcher? = null
    private lateinit var cardData: CardData

    init {
        val year = minExpiry.get(Calendar.YEAR)
        val month = minExpiry.get(Calendar.MONTH)
        minExpiry.clear()
        minExpiry.set(year, month, 1)
        minExpiry.add(Calendar.DAY_OF_MONTH, -1)
        setStyle(STYLE_NO_FRAME, R.style.begateway_MainDialogTheme)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (!isStateSaved)
            activity?.finish()
    }

    override fun onDismiss(dialog: DialogInterface) {
        view?.hideSoftKeyboard()
        super.onDismiss(dialog)
        if (!isStateSaved)
            activity?.finish()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnProgressDialogListener) {
            onProgressDialogListener = context
        }
    }

    override fun onDetach() {
        onProgressDialogListener = null
        super.onDetach()
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
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        setHasOptionsMenu(true)
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                (activity as AbstractActivity?)?.let {
                    it.setToolBar(
                        toolbar
                    ) {
                        dismissAllowingStateLoss()
                    }
                }
            }
            mcbSaveCard.setOnCheckedChangeListener { _, isChecked ->
                PaymentSdk.instance.isSaveCard = isChecked
            }
            mbPay.setOnClickListener {
                pay()
            }
        }
        initCardNumberView()
        initCardNameView()
        initCardExpireDateView()
        initCvcView()
        applyCardTypeValues()
    }

    private fun pay() {
        onProgressDialogListener?.onShowProgress()
        val paymentSdk = PaymentSdk.instance
        paymentSdk.payWithCard(
            PaymentRequest(
                Request(
                    paymentSdk.checkoutWithTokenData.checkout.token,
                    PaymentMethodType.CREDIT_CARD,
                    CreditCard(
                        cardNumber = cardData.cardNumber,
                        verificationValue = cardData.cvcCode,
                        holderName = cardData.cardHolderName,
                        expMonth = cardData.getMonth(),
                        expYear = cardData.getYear()
                    )
                )
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        binding = null
        nfcAdapter = null
        cardNumberInputTypeMask = null
        expiryDateTypeMask = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SCAN_BANK_CARD -> {
                if (resultCode == Activity.RESULT_OK) {
                    applyCardDataFromIntent(data)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun applyCardDataFromIntent(data: Intent?) {
        CardData.getDataFromIntent(data)?.let {
            binding?.apply {
                val pan = it.cardNumber.orEmpty().filter(Char::isDigit)
                setCardType(CardType.getCardTypeByPan(pan))
                tietCardNumber.setText(pan)
                tietCardNumber.onFocusChangeListener?.onFocusChange(
                    tietCardNumber,
                    false
                )
                tietCardName.setText(it.cardHolderName)
                tietCardName.onFocusChangeListener?.onFocusChange(tietCardName, false)
                expiryDateTypeMask?.removeFromTextView()
                tietCardExpiryDate.setText(CardData.getExpiryDateStringForView(it.expiryDate))
                tietCardExpiryDate.onFocusChangeListener?.onFocusChange(
                    tietCardExpiryDate,
                    false
                )
                expiryDateTypeMask?.let { tietCardExpiryDate.installMask(it) }
                tietCvc.setText(it.cvcCode)
                tietCvc.onFocusChangeListener?.onFocusChange(tietCvc, false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.begateway_scan_card_menu, menu)
        menu.findItem(R.id.action_scan_camera).isVisible = activityInfo != null
        menu.findItem(R.id.action_scan_nfc).isVisible = nfcAdapter != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_scan_camera -> {
                intent.component = ComponentName(activityInfo!!.packageName, activityInfo!!.name)
                startActivityForResult(intent, REQUEST_CODE_SCAN_BANK_CARD)
                true
            }
            R.id.action_scan_nfc -> {
                activity?.let {
                    startActivityForResult(
                        NfcScannerActivity.getIntent(it),
                        REQUEST_CODE_SCAN_BANK_CARD
                    )
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateButtonState() {
        binding?.apply {
            mbPay.isEnabled = isAllFieldCorrect()
            if (mbPay.isEnabled) {
                cardData = CardData(
                    if (tilCardNumber.isVisible) {
                        tietCardNumber.text?.toString()
                    } else null,
                    if (tilCardName.isVisible) {
                        tietCardName.text?.toString()
                    } else null,
                    if (tilCardExpiryDate.isVisible) {
                        CardData.getExpiryDateFromString(tietCardExpiryDate.text?.toString())
                    } else null,
                    if (tilCardCvc.isVisible) {
                        tietCvc.text?.toString()
                    } else null,
                )
            }
        }
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
                cardNumberInputTypeMask = maskFormatWatcher(currentCardType.maskFormat)
                installMask(cardNumberInputTypeMask!!)
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
                val firstTwoNumberOfYear = minExpiry.get(Calendar.YEAR) / 100
                expiryDateTypeMask = maskFormatWatcher(
                    "__/${
                        if (firstTwoNumberOfYear == (minExpiry.get(Calendar.YEAR) + MAXIMUM_VALID_YEAR_DIFFERENCE) / 100) {
                            "${firstTwoNumberOfYear}__"
                        } else {
                            "${firstTwoNumberOfYear / 10}___"
                        }
                    }"
                )
                installMask(expiryDateTypeMask!!)
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
                    cardNumberInputTypeMask?.changeMask(
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
            var wrongFilledField: EditText? = null
            val listOfViews = arrayListOf(
                tilCardNumber,
                tilCardName,
                tilCardExpiryDate,
                tilCardCvc
            ).filter { it.isVisible }
            val listSize = listOfViews.size - 1
            listOfViews.forEachIndexed { index, view ->
                val editText = view.editText
                if (wrongFilledField == null && (editText?.text.isNullOrEmpty() || !view.error.isNullOrEmpty())) {
                    wrongFilledField = editText
                }
                if (editText == currentElement && listSize > index) {
                    editText.clearFocus()
                    listOfViews[index + 1].editText?.requestFocus()
                    return true
                } else if (listSize == index) {
                    editText?.clearFocus()
                    wrongFilledField?.requestFocus() ?: editText?.hideSoftKeyboard()
                    return wrongFilledField != null
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

    private fun isCardNumberCorrect(): Boolean {
        val brands = PaymentSdk.instance.checkoutWithTokenData.checkout.brands
        return if (brands.isNullOrEmpty()) {
            true
        } else {
            brands.asSequence().map { it.name }
                .find {
                    currentCardType.name.equals(it, true)
                } != null
        }
                &&
                binding?.tilCardNumber?.editText
                    ?.text?.toString()
                    ?.isCorrectPan(
                        currentCardType.listOfCardNumberSizes,
                        currentCardType.isLunhCheckRequired
                    )
                ?: false
    }

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