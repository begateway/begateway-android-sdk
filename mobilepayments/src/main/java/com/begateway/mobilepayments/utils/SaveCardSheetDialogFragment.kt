package com.begateway.mobilepayments.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ListView
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope

import com.begateway.mobilepayments.R
import com.begateway.mobilepayments.models.network.request.CreditCard
import com.begateway.mobilepayments.models.network.request.PaymentMethodType
import com.begateway.mobilepayments.models.network.request.PaymentRequest
import com.begateway.mobilepayments.models.network.request.Request
import com.begateway.mobilepayments.sdk.PaymentSdk
import com.begateway.mobilepayments.sdk.SaveCardToken
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException

    class SaveCardSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var overlay: FrameLayout
    private lateinit var listView: ListView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val cardToken = arguments?.getString("cardToken")
        val view = inflater.inflate(R.layout.beagateway_token_card_wrap, container, false)
        overlay = view.findViewById(R.id.fl_overlay)
        progressBar = view.findViewById(R.id.progressBar)
        listView = view.findViewById(R.id.listView)
        val goBackButton = view.findViewById<ImageButton>(R.id.goBackButton)
        val addCardButton = view.findViewById<ImageButton>(R.id.addCardButton)

        val creditCardList = getCreditCardList(requireContext())

        val adapter = CreditCardAdapter(requireContext(), R.layout.beagateway_token_card, creditCardList, overlay, progressBar, object : CardDeleteCallback {
            override fun onCardDelete() {
                lifecycleScope.launch(Dispatchers.Main) {
                    dismiss()
                    val newFragment = SaveCardSheetDialogFragment()
                    newFragment.show(parentFragmentManager, newFragment.tag)
                }
            }
        })



        listView.adapter = adapter
        goBackButton.setOnClickListener {
            dismiss()
        }
        listView.adapter = adapter
        addCardButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            overlay.visibility = View.VISIBLE
            dismiss()
            progressBar.visibility = View.GONE
            overlay.visibility = View.GONE
            startActivity(
                context?.let { it1 -> PaymentSdk.getCardFormIntent(it1) }


            )
        }
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedCard = creditCardList[position]
            lifecycleScope.launch(Dispatchers.Main) {
                if (cardToken != null) {
                    selectedCard.token?.let { handleButtonClick(cardToken, it) }
                }
            }
        }
        return view
    }
    private suspend fun handleButtonClick(cardToken: String, token: String) {
        progressBar.visibility = View.VISIBLE
        overlay.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val paymentSdk = PaymentSdk.instance
                context?.let {
                    paymentSdk.payWithCard(
                        requestBody = PaymentRequest(
                            Request(
                                cardToken,
                                PaymentMethodType.CREDIT_CARD,
                                CreditCard(
                                    token = token
                                )
                            )
                        ),
                        context = it
                    )
                }
                Log.d("BeagatewayToken", "Pay with token! $token")
            }finally {
                dialog?.dismiss()
                overlay.visibility = View.GONE
                progressBar.visibility = View.GONE

            }
        }


    }
    private fun getCreditCardList(context: Context): List<SaveCardToken> {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            "Save_Card_Token",
            Context.MODE_PRIVATE
        )

        val existingListJson = sharedPreferences.getString("CreditCardList", null)

        val existingList = mutableListOf<SaveCardToken>()

        existingListJson?.let {
            try {
                val jsonArray = JSONArray(it)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val brand = jsonObject.optString("brand")
                    val last4 = jsonObject.optString("last4")
                    val token = jsonObject.optString("token")
                    val stamp = jsonObject.optString("stamp")
                    existingList.add(SaveCardToken(brand, last4, token, stamp))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        return existingList
    }


}

