package com.begateway.mobilepayments.utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.begateway.mobilepayments.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
interface CardDeleteCallback {
    fun onCardDelete()
}
class CreditCardAdapter(context: Context, private val resource: Int, private val cards: List<SaveCardToken>, private val overlay: FrameLayout,
                        private val progressBar: ProgressBar, private val cardDeleteCallback: CardDeleteCallback)
    : ArrayAdapter<SaveCardToken>(context, resource, cards) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val lastFourTextView: TextView = view.findViewById(R.id.last_four)
        val iconImageView: ImageView = view.findViewById(R.id.icon_card)

        val deleteCardButton: ImageView = view.findViewById(R.id.delete_card)


        val card = cards[position]
        lastFourTextView.text = card.last4

        val cardType = card.brand?.let { getCardTypeFromName(it) }

        val drawableResId = cardType?.let { getDrawableResourceId(it) }
        if (drawableResId != null) {
            iconImageView.setImageResource(drawableResId)
        }
        deleteCardButton.setOnClickListener {
            // Обработчик нажатия на кнопку delete_card
            card.token?.let { it1 -> handleDeleteButtonClick(it1) }
        }

        return view
    }
    fun updateData(newCards: List<SaveCardToken>) {

    }
    private fun handleDeleteButtonClick(token: String) {
        Log.i("MyTag", "Card deleted: $token")
        progressBar.visibility = View.VISIBLE
        overlay.visibility = View.VISIBLE
        // Создание нового списка, исключая карточку с указанным токеном
        val filteredCards = cards.filterNot { it.token == token }.toMutableList()

        // Сохранение обновленного массива в локальном хранилище
        saveCreditCardListToSharedPreferences(filteredCards)
        cardDeleteCallback.onCardDelete()
    }

    private fun saveCreditCardListToSharedPreferences(cards: List<SaveCardToken>) {
        val sharedPreferences = context.getSharedPreferences(
            "Save_Card_Token",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()

        val jsonArray = JSONArray()
        for (card in cards) {
            val jsonObject = JSONObject()
            jsonObject.put("brand", card.brand)
            jsonObject.put("last4", card.last4)
            jsonObject.put("token", card.token)
            jsonObject.put("stamp", card.stamp)
            jsonArray.put(jsonObject)
        }

        editor.putString("CreditCardList", jsonArray.toString())
        editor.apply()
    }
}
    private fun getCardTypeFromName(brand: String): String {
        val brandParts = brand.split("_")
        return if (brandParts.isNotEmpty()) {
            brandParts.last()
        } else {
            ""
        }
    }

    private fun getDrawableResourceId(cardType: String): Int {
        return when (cardType.lowercase(Locale.ROOT)) {
            "amex" -> R.drawable.begateway_ic_amex
            "belkart" -> R.drawable.begateway_ic_belkart
            "dankort" -> R.drawable.begateway_ic_dankort
            "dinersclub" -> R.drawable.begateway_ic_dinersclub
            "discover" -> R.drawable.begateway_ic_discover
            "elo" -> R.drawable.begateway_ic_elo
            "hipercard" -> R.drawable.begateway_ic_hipercard
            "jcb" -> R.drawable.begateway_ic_jcb
            "maestro" -> R.drawable.begateway_ic_maestro
            "mastercard" -> R.drawable.begateway_ic_mastercard
            "mir" -> R.drawable.begateway_ic_mir
            "prostir" -> R.drawable.begateway_ic_prostir
            "rupay" -> R.drawable.begateway_ic_rupay
            "solo" -> R.drawable.begateway_ic_solo
            "switch" -> R.drawable.begateway_ic_switch
            "unionpay" -> R.drawable.begateway_ic_unionpay
            "visa" -> R.drawable.begateway_ic_visa
            else -> R.drawable.begateway_ic_unknown
        }
    }


