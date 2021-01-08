package com.begateway.mobilepayments.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager.LayoutParams.*
import android.widget.FrameLayout
import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.databinding.BegatewayFragmentCardFormBinding
import com.begateway.mobilepayments.utils.hideSoftKeyboard
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CardFormBottomDialog : BottomSheetDialogFragment() {
    private var binding: BegatewayFragmentCardFormBinding? = null
    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.window?.addFlags(FLAG_SECURE)
    }

    override fun onDetach() {
        activity?.window?.clearFlags(FLAG_SECURE)
        super.onDetach()
    }

    init {
        setStyle(STYLE_NO_FRAME, R.style.begateway_MainDialogTheme)
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme).apply {

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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= 30) {
            dialog?.window?.setDecorFitsSystemWindows(false)
            dialog?.window?.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        } else {
            dialog?.window?.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE or SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            (dialog as? BottomSheetDialog)?.also { dialog ->
                val bottomSheet = dialog.findViewById<FrameLayout?>(R.id.design_bottom_sheet)!!
                BottomSheetBehavior.from(bottomSheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isHideable = true
                    peekHeight = 0
                }
            }
        }
        view.viewTreeObserver?.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        binding = null
    }
}