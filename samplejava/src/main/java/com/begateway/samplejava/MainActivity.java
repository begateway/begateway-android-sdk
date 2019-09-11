package com.begateway.samplejava;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.begateway.mobilepayments.OnPaymentResultListener;
import com.begateway.mobilepayments.PaymentBuilder;
import com.begateway.mobilepayments.PaymentModule;
import com.begateway.mobilepayments.model.PaymentResultResponse;
import com.begateway.mobilepayments.model.PaymentTokenResponse;
import com.begateway.mobilepayments.model.ResponseCode;
import com.begateway.mobilepayments.tasks.IRetrievePaymentTokenTask;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements OnPaymentResultListener {

    private PaymentModule paymentModule;

    private EditText editTextToken;

    private RelativeLayout loadingView;

    private ToggleButton toggleButton;

    final private String PUBLIC_STORE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArO7bNKtnJgCn0PJVn2X7QmhjGQ2GNNw412D+NMP4y3Qs69y6i5T/zJBQAHwGKLwAxyGmQ2mMpPZCk4pT9HSIHwHiUVtvdZ/78CX1IQJON/Xf22kMULhquwDZcy3Cp8P4PBBaQZVvm7v1FwaxswyLD6WTWjksRgSH/cAhQzgq6WC4jvfWuFtn9AchPf872zqRHjYfjgageX3uwo9vBRQyXaEZr9dFR+18rUDeeEzOEmEP+kp6/Pvt3ZlhPyYm/wt4/fkk9Miokg/yUPnk3MDU81oSuxAw8EHYjLfF59SWQpQObxMaJR68vVKH32Ombct2ZGyzM7L5Tz3+rkk7C4z9oQIDAQAB";

    final private String PUBLIC_STORE_KEY_3D = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArO7bNKtnJgCn0PJVn2X7QmhjGQ2GNNw412D+NMP4y3Qs69y6i5T/zJBQAHwGKLwAxyGmQ2mMpPZCk4pT9HSIHwHiUVtvdZ/78CX1IQJON/Xf22kMULhquwDZcy3Cp8P4PBBaQZVvm7v1FwaxswyLD6WTWjksRgSH/cAhQzgq6WC4jvfWuFtn9AchPf872zqRHjYfjgageX3uwo9vBRQyXaEZr9dFR+18rUDeeEzOEmEP+kp6/Pvt3ZlhPyYm/wt4/fkk9Miokg/yUPnk3MDU81oSuxAw8EHYjLfF59SWQpQObxMaJR68vVKH32Ombct2ZGyzM7L5Tz3+rkk7C4z9oQIDAQAB";

    final private String YOUR_CHECKOUT_ENDPOINT = "https://checkout.begateway.com/ctp/api/";

    final private String YOUR_RETURN_URL = "https://YOUR_RETURN_URL.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup payment module
        paymentModule = new PaymentBuilder()
                .setTestMode(true)
                .setEndpoint(YOUR_CHECKOUT_ENDPOINT)
                .setReturnUrl(YOUR_RETURN_URL)
                .setDebugMode(true)
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

        Button buttonPayWithCreditCard = findViewById(R.id.button_pay_with_credit_card_token);
        buttonPayWithCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                payWithCreditCardButtonClick();
            }
        });

        Button buttonPayWithEncryptedCreditCard = findViewById(R.id.button_get_payment_token);
        buttonPayWithEncryptedCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getPaymentToken();
            }
        });

        editTextToken = findViewById(R.id.input_payment_token);

        toggleButton = findViewById(R.id.toggle_3d_state);

    }

    void payWithPublicStoreKeyButtonClick(){

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

        paymentModule.payWithPublicKey(toggleButton.isChecked() ? PUBLIC_STORE_KEY_3D : PUBLIC_STORE_KEY, ORDER_JSON);
    }

    void payWithCheckoutJsonButtonClick(){

        String paymentToken = editTextToken.getText().toString();

        if (paymentToken == null || paymentToken.isEmpty())
        {
            showDialog("You need to get PAYMENT_TOKEN");
            return;
        }

        JSONObject CHECKOUT_JSON = null;

        try {
            CHECKOUT_JSON = new JSONObject("{\n" +
                    "    \"checkout\": {\n" +
                    "        \"token\":" + editTextToken.getText().toString() +  ",\n" +
                    "        \"redirect_url\": \"https://checkout.begateway.com/checkout?token=623b342e6fa003ce273c6197380400137057cee5a4640822c2274f0fd3e278e2\",\n" +
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
                    "            \"name\": \"begateway\",\n" +
                    "            \"site\": \"https://begateway.by\"\n" +
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

        String paymentToken = editTextToken.getText().toString();
        String cardToken = toggleButton.isChecked() ? "e94c2a77-5498-45d3-a5b1-3155d0f0bcb3" : "7698e8d7-7394-44c0-8cc4-386eb69e2644";

        if (paymentToken == null || paymentToken.isEmpty())
        {
            showDialog("You need to get PAYMENT_TOKEN");
            return;
        }

        JSONObject CREDIT_CARD_JSON = null;
        try {
            CREDIT_CARD_JSON = new JSONObject("{\n" +
                    "   \"request\": {\n" +
                    "      \"token\":\"" + editTextToken.getText().toString() +  "\",\n" +
                    "      \"payment_method\": \"credit_card\",\n" +
                    "      \"credit_card\": {\n" +
                    "          \"number\": \"*\",\n" +
                    "          \"token\": \""+ cardToken +"\" \n" +
                    "      }\n" +
                    "   }\n" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getLoadingView().setVisibility(View.VISIBLE);

        paymentModule.payWithCreditCard(CREDIT_CARD_JSON);

    }

    void getPaymentToken(){

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

        getLoadingView().setVisibility(View.VISIBLE);

        paymentModule.getPaymentToken(toggleButton.isChecked() ? PUBLIC_STORE_KEY_3D : PUBLIC_STORE_KEY, ORDER_JSON, new IRetrievePaymentTokenTask(){
            @Override
            public void onCallback(PaymentTokenResponse response) {

                if (response.getStatus() == ResponseCode.SUCCESS) {
                    editTextToken.setText(response.getToken());
                } else{
                    showDialog(response.getStatus().toString());
                }

                getLoadingView().setVisibility(View.INVISIBLE);
            }
        });
    }


    private View getLoadingView(){

        if (loadingView == null) {

            loadingView = new RelativeLayout(MainActivity.this);
            loadingView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            loadingView.setBackgroundColor(getResources().getColor(com.begateway.mobilepayments.R.color.begateway_loading));

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            ProgressBar progressBar = new ProgressBar(MainActivity.this);
            progressBar.setLayoutParams(layoutParams);

            loadingView.addView(progressBar);
            loadingView.setGravity(Gravity.CENTER);
            loadingView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            this.addContentView(loadingView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));

            loadingView.setVisibility(View.INVISIBLE);

        }

        return loadingView;
    }

    private void showDialog(String message) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        // Create the AlertDialog object and return it
        AlertDialog showDialog = builder.create();

        showDialog.show();
    }

    @Override
    public void onPaymentResult(PaymentResultResponse paymentResult) {

        getLoadingView().setVisibility(View.INVISIBLE);

        showDialog(paymentResult.getStatus().toString());

        editTextToken.setText(null);

    }
}

