package com.example.eliezer.onlineshop;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Reset_Pass extends AppCompatActivity{

    TextInputEditText emailInputEditText;
    Button sendEmailButton;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset__pass);

        emailInputEditText = (TextInputEditText) findViewById(R.id.emailReset);
        sendEmailButton = (Button) findViewById(R.id.send_email);

        firebaseAuth = FirebaseAuth.getInstance();
        sendEmailButton.setOnClickListener(onClickListener);


    }

    View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (sendEmailButton.equals(view)){
                String email=emailInputEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    emailInputEditText.setError("Required");
                    emailInputEditText.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailInputEditText.setError("Wrong Email Format");
                    emailInputEditText.requestFocus();
                    return;
                }

                //progress alert to the user
                final ProgressDialog progressDialog = new ProgressDialog(Reset_Pass.this);
                progressDialog.setMessage("Sending link to your email...");
                progressDialog.show();

                //planting the email to Firebase
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(Reset_Pass.this,"Check your email",Toast.LENGTH_SHORT).show();
                            emailInputEditText.setText("");
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Reset_Pass.this,"Failed",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
    };
}
