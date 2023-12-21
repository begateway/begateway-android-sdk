package com.begateway.mobilepayments.utils

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout

import androidx.appcompat.app.AppCompatActivity
import com.begateway.mobilepayments.R


class SaveCard : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.begateway_save_card_token)

        val cardToken = findViewById<LinearLayout>(R.id.linearLayoutId)

        cardToken.setOnClickListener{
            Log.d("SaveCard", "OnClickListener works!")
        }
    }
}