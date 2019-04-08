package com.example.eliezer.onlineshop;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogChat extends AppCompatActivity {

    EditText chatEmail,chatPassword;
    Button chatLogin;
    FirebaseAuth mAuth;
    String goRetrieveUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_chat);

        chatEmail = findViewById(R.id.chat_email);
        chatPassword = findViewById(R.id.chat_password);
        chatLogin = findViewById(R.id.chat_sigin);

        goRetrieveUser = getIntent().getStringExtra("send_user_shop");

        mAuth = FirebaseAuth.getInstance();

        chatLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = chatEmail.getText().toString();
                String pass = chatPassword.getText().toString();

                mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener <AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Intent goIntent = new Intent(LogChat.this,chatMain.class);
                            goIntent.putExtra("go_send_user_shop",goRetrieveUser);
                            Toast.makeText(LogChat.this, "User successfully Logged in", Toast.LENGTH_SHORT).show();
                            startActivity(goIntent);
                        }else
                            Toast.makeText(LogChat.this, "Cannot log in", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
