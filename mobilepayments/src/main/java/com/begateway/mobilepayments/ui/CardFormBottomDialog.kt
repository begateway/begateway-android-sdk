package com.begateway.mobilepayments.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.begateway.mobilepayments.BuildConfig
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.databinding.BegatewayFragmentCardFormBinding
import com.begateway.mobilepayments.models.network.request.CreditCard
import com.begateway.mobilepayments.models.network.request.PaymentMethodType
import com.begateway.mobilepayments.models.network.request.PaymentRequest
import com.begateway.mobilepayments.models.network.request.Request
import com.begateway.mobilepayments.models.ui.CardData
import com.begateway.mobilepayments.models.ui.CardType
import com.begateway.mobilepayments.payment.googlepay.GooglePayHelper
import com.begateway.mobilepayments.sdk.PaymentSdk
import com.begateway.mobilepayments.ui.intefaces.OnActionbarSetup
import com.begateway.mobilepayments.ui.intefaces.OnMessageDialogListener
import com.begateway.mobilepayments.ui.intefaces.OnProgressDialogListener
import com.begateway.mobilepayments.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.tinkoff.core.components.nfc.NfcHelper
import ru.tinkoff.core.components.nfc.NfcUtils
import ru.tinkoff.decoro.watchers.DescriptorFormatWatcher
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

private const val REQUEST_CODE_NFC_SETTINGS = 0x55BD

internal class CardFormBottomDialog : BottomSheetDialogFragment() {

    private val minExpiry = Calendar.getInstance()

    //date
    private val expiryFormat =
        SimpleDateFormat(CardData.EXPIRY_DATE_FORMAT_SMALL, Locale.US).apply {
            val now = Calendar.getInstance()
            now.set(Calendar.YEAR, (now.get(Calendar.YEAR) / 100) * 100)
            set2DigitYearStart(now.time)
        }
    private var currentCardType: CardType = CardType.EMPTY
    private val intent = Intent(BuildConfig.SCAN_CARD_BANK_ACTION)
    private var binding: BegatewayFragmentCardFormBinding? = null
    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var activityInfo: ActivityInfo? = null

    //interfaces
    private var onProgressDialogListener: OnProgressDialogListener? = null
    private var onMessageDialogListener: OnMessageDialogListener? = null
    private var onActionbarSetup: OnActionbarSetup? = null

    //nfc
    private var nfcRecognizer: NfcHelper? = null
    private var nfcDialog: AlertDialog? = null
    private var nfcAdapter: NfcAdapter? = null

    private val cardInputFilterList: Array<InputFilter> = arrayOf(
        inputFilterDigits(BuildConfig.BANK_CARD_REGEX),
        InputFilter.LengthFilter(currentCardType.getMaxCardLength())
    )
    private val cvcInputFilterList: Array<InputFilter> = arrayOf(
        inputFilterDigits(BuildConfig.BANK_CARD_REGEX),
        InputFilter.LengthFilter(currentCardType.getMaxCVCLength())
    )
    private var cardNumberInputTypeMask: DescriptorFormatWatcher? = null
    private var expiryDateTypeMask: DescriptorFormatWatcher? = null
    private lateinit var cardData: CardData
    private val scanBankCardLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            CardData.getDataFromIntent(result.data)?.let { applyCardData(it) }
        }
    }

    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            onProgressDialogListener?.onHideProgress()
        }
    }

    init {
        val year = minExpiry.get(Calendar.YEAR)
        val month = minExpiry.get(Calendar.MONTH)
        minExpiry.clear()
        minExpiry.set(year, month, 1)
        minExpiry.add(Calendar.DAY_OF_MONTH, -1)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (!isStateSaved) {
            activity?.finish()
            PaymentSdk.instance.resetValues()
        }
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
        if (context is OnMessageDialogListener) {
            onMessageDialogListener = context
        }
        if (context is OnActionbarSetup) {
            onActionbarSetup = context
        }
        activityInfo = context.findDefaultLocalActivityForIntent(intent)
    }

    override fun onDetach() {
        onProgressDialogListener = null
        onMessageDialogListener = null
        onActionbarSetup = null
        activityInfo = null
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
        val sdkSettings = PaymentSdk.instance.sdkSettings
        if (!sdkSettings.isDebugMode) {
            dialog?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        if (sdkSettings.isNFCScanVisible) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        }
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
        initGooglePayButton()
        binding?.run {
            onActionbarSetup?.addToolBar(toolbar, null, ::dismissAllowingStateLoss)
            mbPay.setOnClickListener {
                pay()
            }
        }
        iniSaveCardCheckBox()
        initCardNumberView()
        initCardNameView()
        initCardExpireDateView()
        initCvcView()
        applyCardTypeValues()
    }

    private fun initGooglePayButton() {
        try {
            val context = context ?: return
            val metadata = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            ).metaData
            if (
                PaymentSdk.instance.checkoutWithTokenData!!.checkout.googlePay != null
                && metadata.getBoolean(
                    "com.google.android.gms.wallet.api.enabled"
                )
            ) {
                GooglePayHelper.checkIsReadyToPayTask(requireActivity(), ::updateGooglePayButton)
            }
        } catch (ex: PackageManager.NameNotFoundException) {
        }
    }

    private fun updateGooglePayButton(isSuccess: Boolean) {
        binding?.run {
            mbGooglePay.isVisible = isSuccess
            if (isSuccess) {
                mbGooglePay.setOnClickListener {
                    onProgressDialogListener?.onShowProgress()
                    CoroutineScope(Dispatchers.Main).launch {
                        val orderDetails = PaymentSdk.instance.getOrderDetails()
                        if (orderDetails != null) {
                            GooglePayHelper.startPaymentFlow(
                                requireActivity(),
                                GOOGLE_PAY_RETURN_CODE,
                                orderDetails
                            )
                        } else {
                            onProgressDialogListener?.onHideProgress()
                        }
                    }
                }
            }
        }
    }

    private fun pay() {
        onProgressDialogListener?.onShowProgress()
        val paymentSdk = PaymentSdk.instance
        CoroutineScope(Dispatchers.IO).launch {
            paymentSdk.payWithCard(
                requestBody = PaymentRequest(
                    Request(
                        token = paymentSdk.checkoutWithTokenData!!.checkout.token,
                        paymentMethod = PaymentMethodType.CREDIT_CARD,
                        creditCard = CreditCard(
                            cardNumber = cardData.cardNumber,
                            cvc = cardData.cvcCode,
                            holderName = cardData.cardHolderName,
                            expMonth = cardData.getMonth(),
                            expYear = cardData.getYear(),
                            isSaveCard = paymentSdk.isSaveCard
                        )
                    )
                ),
                context = requireActivity(),
                launcher = paymentLauncher
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        binding = null
        nfcAdapter = null
        cardNumberInputTypeMask = null
        expiryDateTypeMask = null
    }

    override fun onDestroy() {
        nfcRecognizer = null
        super.onDestroy()
    }

    private fun applyCardData(cardData: CardData) {
        binding?.apply {
            val pan = cardData.cardNumber.orEmpty().filter(Char::isDigit)
            setCardType(CardType.getCardTypeByPan(pan))
            tietCardNumber.setText(pan)
            tietCardNumber.onFocusChangeListener?.onFocusChange(
                tietCardNumber,
                false
            )
            tietCardName.setText(cardData.cardHolderName)
            tietCardName.onFocusChangeListener?.onFocusChange(tietCardName, false)
            if (minExpiry.time < cardData.expiryDate) {
                expiryDateTypeMask?.removeFromTextView()
                tietCardExpiryDate.setText(CardData.getExpiryDateStringForView(cardData.expiryDate))
                expiryDateTypeMask?.let { tietCardExpiryDate.installMask(it) }
            }
            tietCardExpiryDate.onFocusChangeListener?.onFocusChange(
                tietCardExpiryDate,
                false
            )
            tietCvc.setText(cardData.cvcCode)
            tietCvc.onFocusChangeListener?.onFocusChange(tietCvc, false)
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
                scanBankCardLauncher.launch(intent)
                true
            }
            R.id.action_scan_nfc -> {
                initNfcScanning()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initNfcScanning() {
        val appCompatActivity = requireActivity() as? AppCompatActivity
        if (nfcRecognizer == null && appCompatActivity != null) {
            nfcRecognizer =
                NfcHelper.create(appCompatActivity, object : NfcHelper.Callback {
                    override fun onResult(bundle: Bundle) {
                        nfcScanningComplete()
                        applyCardData(
                            CardData(
                                cardNumber = bundle.getString(NfcHelper.CARD_NUMBER),
                                expiryDate = CardData.getExpiryDateFromString(
                                    bundle.getString(
                                        NfcHelper.EXPIRY_DATE
                                    )
                                )
                            )
                        )
                    }


                    override fun onException(p0: java.lang.Exception?) = onException()

                    override fun onNfcNotSupported() = onNfcNotSupportedMessage()

                    override fun onNfcDisabled() = onNfcDisabledMessage()
                })
        }
        nfcRecognizer?.startListening()
        if (nfcRecognizer != null && nfcAdapter?.isEnabled == true) {
            nfcDialog = onMessageDialogListener?.showMessageDialog(
                requireContext(),
                messageId = R.string.begateway_nfc_attach_bank_card,
                positiveButtonTextId = R.string.begateway_cancel,
                positiveOnClick = ::nfcScanningComplete
            )
        }
    }

    private fun updateCardState() {
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

    private fun iniSaveCardCheckBox() {
        binding?.run {
            val instance = PaymentSdk.instance
            val saveCardFieldVisible =
                instance.paymentData?.checkout?.settings?.saveCardPolicy?.customerContract == true
            mcbSaveCard.isVisible = saveCardFieldVisible
            if (saveCardFieldVisible) {
                mcbSaveCard.setOnCheckedChangeListener { _, isChecked ->
                    instance.isSaveCard = isChecked
                }
            }
        }
    }

    private fun initCardNumberView() {
        binding?.run {
            val cardNumberFieldVisible = PaymentSdk.instance.sdkSettings.isCardNumberFieldVisible
            tilCardNumber.isVisible = cardNumberFieldVisible
            if (cardNumberFieldVisible) {
                tilCardNumber.apply {
                    requestFocus()
                    setStartIconTintList(null)
                    onFocusListener(
                        ::isCardNumberCorrect
                    ) { getString(R.string.begateway_card_number_invalid) }
                    onTextChanged(
                        ::updateCardState,
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
    }

    private fun isCardMaxLengthAccepted(length: Int) = currentCardType.getMaxCardLength() == length

    private fun initCardNameView() {
        binding?.run {
            val cardHolderFieldVisible = PaymentSdk.instance.sdkSettings.isCardHolderFieldVisible
            tilCardName.isVisible = cardHolderFieldVisible
            if (cardHolderFieldVisible) {
                tilCardName.apply {
                    onFocusListener(
                        ::isCardNameCorrect
                    ) { getString(R.string.begateway_cardholder_name_required) }
                    onTextChanged(
                        ::updateCardState,
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
    }

    private fun initCardExpireDateView() {
        binding?.run {
            val isCardDateFieldVisible = PaymentSdk.instance.sdkSettings.isCardDateFieldVisible
            tilCardExpiryDate.isVisible = isCardDateFieldVisible
            if (isCardDateFieldVisible) {
                tilCardExpiryDate.apply {
                    onFocusListener(
                        ::isExpiryCorrect
                    ) { getString(R.string.begateway_expiration_invalid) }
                    onTextChanged(
                        ::updateCardState,
                        ::requestFocusToNextVisibleElement,
                        ::isCardExpireLengthAccepted
                    )
                    onExpiryTextChanged()
                }
                tietCardExpiryDate.run {
                    onEditorListener(::requestFocusToNextVisibleElement)
                    expiryDateTypeMask = maskFormatWatcher("__/__")
                    installMask(expiryDateTypeMask!!)
                }
            }
        }
    }

    private fun isCardExpireLengthAccepted(length: Int) = BuildConfig.EXPIRY_DATE_LENGTH == length

    private fun initCvcView() {
        binding?.run {
            val isCardCVCFieldVisible = PaymentSdk.instance.sdkSettings.isCardCVCFieldVisible
            tilCardCvc.isVisible = isCardCVCFieldVisible
            if (isCardCVCFieldVisible) {
                tilCardCvc.apply {
                    onFocusListener(
                        ::isCVCCorrect,
                        ::getCvcError
                    )
                    onTextChanged(
                        ::updateCardState,
                        ::requestFocusToNextVisibleElement,
                        ::isCardCvcLengthAccepted
                    )
                }

                tietCvc.onEditorListener(::requestFocusToNextVisibleElement)
            }
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
            !binding?.tilCardName?.editText?.text.isNullOrEmpty()
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
        if (binding?.tilCardNumber?.isVisible == true) {
            binding?.tilCardNumber?.editText
                ?.text?.toString()
                ?.isCorrectPan(
                    currentCardType.listOfCardNumberSizes,
                    currentCardType.isLunhCheckRequired
                )
                ?: false
        } else {
            true
        }


    private fun isExpiryCorrect(): Boolean =
        if (binding?.tilCardExpiryDate?.isVisible == true) {
            val editText = binding?.tilCardExpiryDate?.editText
            editText?.length() == BuildConfig.EXPIRY_DATE_LENGTH &&
                    try {
                        val text = editText.text?.toString()
                        !text.isNullOrEmpty() && minExpiry.time < expiryFormat.parse(text)
                    } catch (e: ParseException) {
                        false
                    }
        } else {
            true
        }

    private fun onException() {
        onMessageDialogListener?.showMessageDialog(
            requireContext(),
            titleId = R.string.begateway_error,
            messageId = R.string.begateway_error_during_scanning,
            positiveOnClick = ::nfcScanningComplete
        )
    }

    private fun onNfcDisabledMessage() {
        onMessageDialogListener?.showMessageDialog(
            requireContext(),
            R.string.begateway_error,
            R.string.begateway_turn_on_nfc_message,
            R.string.begateway_turn_on,
            R.string.begateway_cancel,
            { dialog, _ ->
                dialog.dismiss()
                NfcUtils.openNfcSettingsForResult(requireActivity(), REQUEST_CODE_NFC_SETTINGS)
            },
            ::nfcScanningComplete
        )
    }

    private fun onNfcNotSupportedMessage() {
        onMessageDialogListener?.showMessageDialog(
            requireContext(),
            titleId = R.string.begateway_error,
            messageId = R.string.begateway_nfc_not_supported_message,
            positiveOnClick = ::nfcScanningComplete
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun nfcScanningComplete(dialog: DialogInterface? = null, which: Int? = null) {
        nfcRecognizer?.stopListening()
        nfcRecognizer?.destroy()
        nfcRecognizer = null
        nfcDialog?.dismiss()
        dialog?.dismiss()
    }
}