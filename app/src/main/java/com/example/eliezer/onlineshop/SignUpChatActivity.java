package com.example.eliezer.onlineshop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SignUpChatActivity extends AppCompatActivity {
    EditText userName, userPassword, userEmail;
    Button signUp,signIn;
    DatabaseReference mSignupDatabase;
    DatabaseReference mUsersDatabase;
    FirebaseAuth mAuth;
    String uName,uPass,uEmail, current_uid;
    ProgressDialog progressDialog;
    RelativeLayout relativeLayout;
    String chat_shop_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_chat);

        userName = findViewById(R.id.signName);
        userPassword = findViewById(R.id.signPassword);
        userEmail = findViewById(R.id.signEmail);

        signUp = findViewById(R.id.signChatButton);
        signIn = findViewById(R.id.signInChat);
        relativeLayout = findViewById(R.id.relativeChat);

        chat_shop_user = getIntent().getStringExtra("chat_shop_user_id");

        mAuth = FirebaseAuth.getInstance();

        mSignupDatabase = FirebaseDatabase.getInstance().getReference().child("ChatUsers");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        progressDialog = new ProgressDialog(SignUpChatActivity.this);


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uName = userName.getText().toString().trim();

                uPass = userPassword.getText().toString().trim();

                uEmail = userEmail.getText().toString().trim();

                //progressDialog.setTitle("Signing to chat");
                progressDialog.setMessage("Waiting for internet, don't cancel");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();
              if (!TextUtils.isEmpty(uName) || !TextUtils.isEmpty(uPass) || !TextUtils.isEmpty(uEmail)){

                    mAuth.createUserWithEmailAndPassword(uEmail,uPass).addOnCompleteListener(new OnCompleteListener <AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                HashMap<String,String> signUpMap = new HashMap <>();
                                signUpMap.put("ChatUserName",uName);
                                signUpMap.put("ChatUserPassword",uPass);
                                signUpMap.put("ChatUserEmail",uEmail);
                                signUpMap.put("ChatUserID",current_uid);

                                mSignupDatabase.setValue(signUpMap).addOnCompleteListener(new OnCompleteListener <Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task <Void> task) {
                                        if (task.isSuccessful()){
                                            checkUserExists();
                                        }
                                    }
                                });
                            }else{
                                //Toast.makeText(SignUpChatActivity.this, "Sign Up fail", Toast.LENGTH_SHORT).show();
                                Snackbar.make(relativeLayout,"SignUp Failed",Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        return;
                                    }
                                });
                                progressDialog.dismiss();
                            }
                        }
                    });
                }else {
                    progressDialog.dismiss();
                    Toast.makeText(SignUpChatActivity.this, "Empty field", Toast.LENGTH_SHORT).show();
                }

            }
        });



        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent chatIntent = new Intent(SignUpChatActivity.this,LogChat.class);
               chatIntent.putExtra("send_user_shop",chat_shop_user);
               startActivity(chatIntent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser mUser = mAuth.getCurrentUser();

        if (mUser != null){
            current_uid = mAuth.getCurrentUser().getUid().toString();
        }
    }


    public void checkUserExists(){
        final String uid = mAuth.getCurrentUser().getUid();

        mUsersDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid)) {
                    progressDialog.dismiss();
                    Toast.makeText(SignUpChatActivity.this, "Sign up Successful ", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpChatActivity.this,chatMain.class));
                }else {
                    Toast.makeText(SignUpChatActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
