package com.skilex.customer.ccavenue.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.skilex.customer.R;
import com.skilex.customer.activity.AddressActivity;
import com.skilex.customer.activity.MainActivity;
import com.skilex.customer.ccavenue.utility.AvenuesParams;
import com.skilex.customer.ccavenue.utility.ServiceUtility;
import com.skilex.customer.utils.PreferenceStorage;

import java.util.Random;


public class InitialScreenActivity extends AppCompatActivity {

    private EditText accessCode, merchantId, currency, amount, orderId, rsaKeyUrl, redirectUrl, cancelUrl;
    private TextView amountDisplay;
    String page;

    private void init() {
        accessCode = (EditText) findViewById(R.id.accessCode);
        merchantId = (EditText) findViewById(R.id.merchantId);
        orderId = (EditText) findViewById(R.id.orderId);
        currency = (EditText) findViewById(R.id.currency);
        amount = (EditText) findViewById(R.id.amount);
        rsaKeyUrl = (EditText) findViewById(R.id.rsaUrl);
        redirectUrl = (EditText) findViewById(R.id.redirectUrl);
        cancelUrl = (EditText) findViewById(R.id.cancelUrl);
        amountDisplay = (TextView) findViewById(R.id.amount_display);

        String adv = (String) getIntent().getSerializableExtra("advpay");
        page = (String) getIntent().getSerializableExtra("page");

        if (page.equalsIgnoreCase("advance_pay")) {
            redirectUrl.setText(R.string.redirect_url_advance);
            cancelUrl.setText(R.string.redirect_url_advance);
        } else {
            redirectUrl.setText(R.string.redirect_url);
            cancelUrl.setText(R.string.redirect_url);
        }
        amount.setText(adv);
        amountDisplay.setText("₹ "+ adv);
        orderId.setText(PreferenceStorage.getOrderId(this));

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getApplicationContext());
                alertDialogBuilder.setTitle("Payment");
                alertDialogBuilder.setMessage("Are you sure you want to cancel your order?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialogBuilder.show();
            }

        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_screen);
        init();
    }

    public void onClick(View view) {
        //Mandatory parameters. Other parameters can be added if required.
        String vAccessCode = ServiceUtility.chkNull(accessCode.getText()).toString().trim();
        String vMerchantId = ServiceUtility.chkNull(merchantId.getText()).toString().trim();
        String vCurrency = ServiceUtility.chkNull(currency.getText()).toString().trim();
        String vAmount = ServiceUtility.chkNull(amount.getText()).toString().trim();
        if (!vAccessCode.equals("") && !vMerchantId.equals("") && !vCurrency.equals("") && !vAmount.equals("")) {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(AvenuesParams.ACCESS_CODE, ServiceUtility.chkNull(accessCode.getText()).toString().trim());
            intent.putExtra(AvenuesParams.MERCHANT_ID, ServiceUtility.chkNull(merchantId.getText()).toString().trim());
            intent.putExtra(AvenuesParams.ORDER_ID, ServiceUtility.chkNull(orderId.getText()).toString().trim());
            intent.putExtra(AvenuesParams.CURRENCY, ServiceUtility.chkNull(currency.getText()).toString().trim());
            intent.putExtra(AvenuesParams.AMOUNT, ServiceUtility.chkNull(amount.getText()).toString().trim());

            intent.putExtra(AvenuesParams.REDIRECT_URL, ServiceUtility.chkNull(redirectUrl.getText()).toString().trim());
            intent.putExtra(AvenuesParams.CANCEL_URL, ServiceUtility.chkNull(cancelUrl.getText()).toString().trim());
            intent.putExtra(AvenuesParams.RSA_KEY_URL, ServiceUtility.chkNull(rsaKeyUrl.getText()).toString().trim());

            intent.putExtra("page", page);

            startActivity(intent);
        } else {
            showToast("All parameters are mandatory.");
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, "Toast: " + msg, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        //generating new order number for every transaction
        Integer randomNum = ServiceUtility.randInt(0, 9999999);
        orderId.setText(randomNum.toString());
    }

}