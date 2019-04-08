package com.example.eliezer.onlineshop;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Button send;
    private Button see_shops;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mDatabase;
    private String current_status;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.keepSynced(true);


        send=findViewById(R.id.create_shop);

        see_shops=findViewById(R.id.see_shop);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //IF USER HAS ALREADY LOGGED IN, HE SHALL BE ABLE TO ADD ARTICLES
                //IF NOT HE SHALL LOG IN FIRST

                CheckUserExists();
             }
        });


        see_shops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentMainHome=new Intent(MainActivity.this,MainHome.class);
                startActivity(intentMainHome);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            current_status = "exists";
        }else{
            current_status = "not_exists";
        }

    }

    public void CheckUserExists(){
       // final String current_user = mAuth.getCurrentUser().getUid().toString();

        if (current_status.equals("exists")){
            Intent goToLogin = new Intent(MainActivity.this,ArticleActivity.class);
            startActivity(goToLogin);
        }else{
            Intent intent=new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);

        }
    }
}
