package com.example.eliezer.onlineshop;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SendEmailActivity extends AppCompatActivity {

    private EditText emailCustomer,emailSubject,emailContent;

    private Button sendEmail;

    private DatabaseReference mEmailUserDatabase;

    private String extract_email,extract_user,extract_product,extract_user_id;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email);

        emailContent = findViewById(R.id.email_content);
        emailSubject = findViewById(R.id.email_subject);
        emailCustomer = findViewById(R.id.customer_email);
        sendEmail = findViewById(R.id.send_email);

        mToolbar = findViewById(R.id.include);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Contact Shop Owner");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        extract_user = getIntent().getStringExtra("extract_email_of_user");
        extract_product = getIntent().getStringExtra("product_id");
        extract_user_id = getIntent().getStringExtra("product_user");

        Toast.makeText(SendEmailActivity.this, extract_user, Toast.LENGTH_SHORT).show();

        mEmailUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(extract_user);
        mEmailUserDatabase.keepSynced(true);

        mEmailUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                extract_email = dataSnapshot.child("Email").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent emailIntent = new Intent(Intent.ACTION_SEND);
                //4567/emailIntent.setData(Uri.parse("email"));
                String [] emailSend = {"eliezer.basubi30@gmail.com","eliezer.basubi30@gmail.com"};
                String sender = emailCustomer.getText().toString();
                //String To [] = {extract_email,sender};
                String To [] = {extract_email};

                String subj = emailSubject.getText().toString();
                String body = emailContent.getText().toString();

                sentEmailMethod(SendEmailActivity.this,To,subj,body);

                //Clear fields when message sent
                clearText();

                /*Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL,emailSend);
                //intent.putExtra(Intent.EXTRA_CC,extract_email);
                intent.putExtra(Intent.EXTRA_SUBJECT, subj);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                intent.setData(Uri.parse("mailto:"+extract_email)); // or just "mailto:" for blank
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
                startActivity(intent);*/
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent emailIntent = new Intent(this, DetailsActivity.class);
        emailIntent.putExtra("product_id",extract_product);
        emailIntent.putExtra("product_user",extract_user_id);

        startActivity(emailIntent);
    }

    public static void sentEmailMethod(Context mContext, String[] addresses, String subject, String body) {

        try {
            Intent sendIntentGmail = new Intent(Intent.ACTION_SENDTO);
            sendIntentGmail.setType("plain/text");
            sendIntentGmail.setData(Uri.parse(TextUtils.join(",", addresses)));
            sendIntentGmail.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
            sendIntentGmail.putExtra(Intent.EXTRA_EMAIL, addresses);
            if (subject != null) sendIntentGmail.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (body != null) sendIntentGmail.putExtra(Intent.EXTRA_TEXT, body);
            sendIntentGmail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(sendIntentGmail);
        } catch (Exception e) {
            //When Gmail App is not installed or disable
            Intent sendIntentIfGmailFail = new Intent(Intent.ACTION_SEND);
            sendIntentIfGmailFail.setType("*/*");
            sendIntentIfGmailFail.putExtra(Intent.EXTRA_EMAIL, addresses);
            if (subject != null) sendIntentIfGmailFail.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (body != null) sendIntentIfGmailFail.putExtra(Intent.EXTRA_TEXT, body);
            if (sendIntentIfGmailFail.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(sendIntentIfGmailFail);
            }
        }
    }

    public void clearText(){
        emailCustomer.getText().clear();
        emailSubject.getText().clear();
        emailContent.getText().clear();
    }
}
