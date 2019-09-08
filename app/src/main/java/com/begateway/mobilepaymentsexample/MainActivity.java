package com.begateway.mobilepaymentsexample;

import android.os.Bundle;
import android.util.Log;
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
                .setUseEnctyptedCard(true)
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

        Button buttonPayWithEncryptedCreditCard = findViewById(R.id.button_pay_with_encrypted_credit_card_json);
        buttonPayWithEncryptedCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                payWithEncryptedCreditCardButtonClick();
            }
        });

        editTextToken = findViewById(R.id.input_payment_token);

        editTextPublicStoreKey = findViewById(R.id.input_public_store_key);

        editTextPublicStoreKey.setText("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvextn45qf3NiNzqBYXMvcaSFlgoYE/LDuDDHtNNM4iWJP7BvjBkPcZu9zAfo5IiMxl660r+1E4PYWwr0iKSQ8+7C/WcSYwP8WlQVZH+2KtPmJgkPcBovz3/aZrQpj6krcKLklihg3Vs++TtXAbpCCbhIq0DJ3T+khttBqTGD+2x2vOC68xPgMwvnwQinfhaHEQNbtEcWWXPw9LYuOTuCwKlqijAEds4LgKSisubqrkRw/HbAKVfa659l5DJ8QuXctjp3Ic+7P2TC+d+rcfylxKw9c61ykHS1ggI/N+/KmEDVJv1wHvdy7dnT0D/PhArnCB37ZDAYErv/NMADz2/LuQIDAQAB");

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

    void payWithEncryptedCreditCardButtonClick(){

        JSONObject CREDIT_CARD_JSON = null;

        String publicKey = editTextPublicStoreKey.getText().toString();

        try {
            CREDIT_CARD_JSON = new JSONObject("{\n" +
                    "   \"request\":{\n" +
                    "      \"token\":\"" + editTextToken.getText().toString() +  "\",\n" +
                    "      \"payment_method\": \"credit_card\",\n" +
                    "      \"encrypted_credit_card\":{\n" +
                    "         \"number\":\""+ paymentModule.encryptCardData("4200000000000000", publicKey) +"\",\n" +
                    "         \"verification_value\":\"" + paymentModule.encryptCardData("123", publicKey) +"\",\n" +
                    "         \"holder\":\""+ paymentModule.encryptCardData("IVAN IVANOV", publicKey) +"\",\n" +
                    "         \"exp_month\":\""+ paymentModule.encryptCardData("01", publicKey) +"\",\n" +
                    "         \"exp_year\":\""+ paymentModule.encryptCardData("2020", publicKey) +"\"\n" +
                    "      }\n" +
                    "   }\n" +
                    "}");

            Log.e("ENCRYPT", CREDIT_CARD_JSON.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        paymentModule.payWithCreditCard(CREDIT_CARD_JSON);
    }

    @Override
    public void onPaymentResult(PaymentResultResponse paymentResult) {


    }
}
