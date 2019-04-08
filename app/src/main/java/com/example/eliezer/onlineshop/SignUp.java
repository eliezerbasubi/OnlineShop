package com.example.eliezer.onlineshop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private TextInputEditText fname;
    private TextInputEditText oname;
    private TextInputEditText phone;
    private TextInputEditText email;
    private TextInputEditText password;
    private Button signup;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference firebaseDatabase;
    private DatabaseReference firebaseCheck;
    private Toolbar signToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signToolBar = (Toolbar)findViewById(R.id.app_bar_layout);
        setSupportActionBar(signToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        fname = (TextInputEditText)findViewById(R.id.firstname);
        oname = (TextInputEditText)findViewById(R.id.othername);
        phone = (TextInputEditText)findViewById(R.id.phonenumber);
        email = (TextInputEditText)findViewById(R.id.email);
        password = (TextInputEditText)findViewById(R.id.password);
        signup = (Button)findViewById(R.id.signup);


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstname = fname.getText().toString();
                String othername = oname.getText().toString();
                String phonenumber = phone.getText().toString();
                String emailText = email.getText().toString();
                String pass = password.getText().toString();

                if(TextUtils.isEmpty(firstname)){
                    fname.setError("required");
                    fname.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(othername)){
                    oname.setError("required");
                    oname.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(phonenumber)){
                    phone.setError("required");
                    phone.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(emailText)){
                    email.setError("required");
                    email.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(pass)){
                    password.setError("required");
                    password.requestFocus();
                    return;
                }

                progressDialog.setMessage(" processing ...");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();
                registerUser(firstname,othername,phonenumber,emailText,pass);

            }


        });




    }

    private void registerUser(final String fname, final String oname, final String phone, final String email, final String password) {

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    final String uid = current_user.getUid();
                    firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String,String> userMap= new HashMap<>();
                    userMap.put("Firstname",fname);
                    userMap.put("Othername",oname);
                    userMap.put("Phone",phone);
                    userMap.put("Email",email);
                    userMap.put("Password",password);

                    firebaseDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            setFreeFields();
                            startActivity(new Intent(SignUp.this,OwnerLog.class));

                        }
                    });

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUp.this,"error" +e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setFreeFields(){
        fname.setText("");
        oname.setText("");
        phone.setText("");
        email.setText("");
        password.setText("");

    }
}