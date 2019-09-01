package com.begateway.mobilepaymentsexample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.begateway.mobilepayments.OnPaymentResultListener;
import com.begateway.mobilepayments.PaymentBuilder;
import com.begateway.mobilepayments.PaymentModule;
import com.begateway.mobilepayments.model.PaymentResultResponse;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements OnPaymentResultListener {

    private PaymentModule paymentModule;

    private EditText editTextToken;

    private EditText editTextPublicStoreKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup payment module
        paymentModule = new PaymentBuilder()
                .setTestMode(true)
                .setEndpoint("https://checkout.bepaid.by/ctp/api/")
                .setPaymentResultListener(MainActivity.this)
                .build(getApplicationContext(), MainActivity.this);


        Button buttonPayWithPublicStoreKey = findViewById(R.id.button_pay_with_public_store_key);
        buttonPayWithPublicStoreKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                payWithPublicStoreKeyButtonClick();
            }
        });

        Button buttonPayWithCheckoutJson = findViewById(R.id.button_pay_with_checkout_json);
        buttonPayWithCheckoutJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                payWithCheckoutJsonButtonClick();
            }
        });

        Button buttonPayWithCreditCard = findViewById(R.id.button_pay_with_credit_card_json);
        buttonPayWithCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                payWithCreditCardButtonClick();
            }
        });

        editTextToken = findViewById(R.id.input_payment_token);

        editTextPublicStoreKey = findViewById(R.id.input_public_store_key);

    }

    void payWithPublicStoreKeyButtonClick(){

        String PUBLIC_STORE_KEY = editTextPublicStoreKey.getText().toString();

        JSONObject ORDER_JSON = null;

        try {
            ORDER_JSON = new JSONObject("{\n" +
                    "   \"amount\": \"100\",\n" +
                    "   \"currency\": \"USD\",\n" +
                    "   \"description\": \"Payment description\", \n" +
                    "   \"tracking_id\" : \"merchant_id\", \n" +
                    "   \"additional_data\": {\n" +
                    "      \"contract\": [ \"recurring\", \"card_on_file\" ] \n" +
                    "   }\n" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        paymentModule.payWithPublicKey(PUBLIC_STORE_KEY, ORDER_JSON);
    }

    void payWithCheckoutJsonButtonClick(){

        JSONObject CHECKOUT_JSON = null;

        try {
            CHECKOUT_JSON = new JSONObject("{\n" +
                    "    \"checkout\": {\n" +
                    "        \"token\":" + editTextToken.getText().toString() +  ",\n" +
                    "        \"redirect_url\": \"https://checkout.bepaid.by/checkout?token=623b342e6fa003ce273c6197380400137057cee5a4640822c2274f0fd3e278e2\",\n" +
                    "        \"brands\": [\n" +
                    "            {\n" +
                    "                \"alternative\": false,\n" +
                    "                \"name\": \"visa\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"alternative\": false,\n" +
                    "                \"name\": \"master\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"alternative\": false,\n" +
                    "                \"name\": \"belkart\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"alternative\": false,\n" +
                    "                \"name\": \"visa\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"alternative\": false,\n" +
                    "                \"name\": \"master\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"alternative\": false,\n" +
                    "                \"name\": \"maestro\"\n" +
                    "            }\n" +
                    "        ],\n" +
                    "        \"company\": {\n" +
                    "            \"name\": \"bePaid\",\n" +
                    "            \"site\": \"https://bepaid.by\"\n" +
                    "        },\n" +
                    "        \"description\": \"Payment description\",\n" +
                    "        \"card_info\": {}\n" +
                    "    }\n" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        paymentModule.payWithCheckoutJson(CHECKOUT_JSON);
    }

    void payWithCreditCardButtonClick(){

        JSONObject CREDIT_CARD_JSON = null;

        try {
            CREDIT_CARD_JSON = new JSONObject("{\n" +
                    "   \"request\":{\n" +
                    "      \"token\":" + editTextToken.getText().toString() +  ",\n" +
                    "      \"payment_method\": \"credit_card\",\n" +
                    "      \"credit_card\":{\n" +
                    "         \"number\":\"4200000000000000\",\n" +
                    "         \"verification_value\":\"123\",\n" +
                    "         \"holder\":\"IVAN IVANOV\",\n" +
                    "         \"exp_month\":\"01\",\n" +
                    "         \"exp_year\":\"2020\"\n" +
                    "      }\n" +
                    "   }\n" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        paymentModule.payWithCreditCard(CREDIT_CARD_JSON);
    }

    @Override
    public void onPaymentResult(PaymentResultResponse paymentResult) {


    }
}
