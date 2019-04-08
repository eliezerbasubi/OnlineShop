package com.example.eliezer.onlineshop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout password;
    private TextInputLayout email;
    private Button login;
    TextView register,reset;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    //method for validating email
    public boolean ValidateEmailPattern(String userEmail){
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(userEmail).matches();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        //checking if the user is login
        if(mAuth.getCurrentUser() != null){
            Intent loginIntent = new Intent(LoginActivity.this,ArticleActivity.class);

            //Keeps the user online once he has logged in
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }

        email = (TextInputLayout) findViewById(R.id.user_email);
        password = (TextInputLayout) findViewById(R.id.user_password);
        login = (Button) findViewById(R.id.loginBtn);
        register = (TextView) findViewById(R.id.register);
        reset = (TextView) findViewById(R.id.reset_password);

        progressDialog = new ProgressDialog(this);

        login.setOnClickListener(onClickListener);
        reset.setOnClickListener(onClickListener);
        register.setOnClickListener(onClickListener);

    }



    View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (login.equals(view)){

                String my_email = email.getEditText().getText().toString().trim();
                String mypassword = password.getEditText().getText().toString().trim();

                if(my_email.isEmpty()){
                    email.setError("required");
                    email.requestFocus();
                    return;
                }

                if(!ValidateEmailPattern(my_email)){
                    email.setError("Enter valid email");
                    email.requestFocus();
                }

                if(mypassword.isEmpty()){
                    password.setError("required");
                    password.requestFocus();
                    return;
                }

                if(!TextUtils.isEmpty(my_email) || !TextUtils.isEmpty(mypassword)) {


                    progressDialog.setMessage("Sign in");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    loginUser(my_email,mypassword);

                }
            }else if(register.equals(view)){
                startActivity(new Intent(LoginActivity.this,SignUp.class));

            } else if(reset.equals(view)){
                startActivity(new Intent(LoginActivity.this,Reset_Pass.class));
            }

        }
    };

    //method for sign in
    private void loginUser(String email, String password){

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    progressDialog.dismiss();
                    Intent loginIntent = new Intent(LoginActivity.this,ArticleActivity.class);

                    //Keeps the user online once he has logged in
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                    finish();

                }else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Password invalid", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


}
