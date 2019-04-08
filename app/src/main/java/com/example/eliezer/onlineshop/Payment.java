package com.example.eliezer.onlineshop;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import java.math.BigDecimal;

public class Payment extends AppCompatActivity {

    Button paypal,call;
    PayPalConfiguration m_Configuration;
    String m_ClientID ="AXELKmvq1aLs_3uROg-ZC50twkT4PbOV9VchJDduoQs2uoyHqrA4RvPhOZ3Qq0cLB8w7TjrkxwZm89fY";
    Intent m_Service;
    int m_paypapRequestCode = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        paypal=(Button)findViewById(R.id.paypal);
        call=(Button)findViewById(R.id.call);

        m_Configuration = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                .clientId(m_ClientID);
        m_Service = new Intent(this, PayPalService.class);
        m_Service.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, m_Configuration);
        startService(m_Service);
    }//end of oncreate

    //triggered by call in xml->button ID->paypal
    public void pay(View view) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(100), "USD", "KUIRMA SHOP PAYMENT",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, m_Configuration);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, m_paypapRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == m_paypapRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    String state = confirmation.getProofOfPayment().getState();
                    if (state.equals("approved"))
                        Toast.makeText(Payment.this,"Payment Approved",Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(Payment.this,"Error in payment Try Again",Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(Payment.this,"Confirmation is null",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}

