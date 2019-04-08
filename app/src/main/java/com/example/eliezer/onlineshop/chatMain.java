package com.example.eliezer.onlineshop;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class chatMain extends AppCompatActivity {
    EditText input_messages;
    ImageButton send;
    String retrieve_shop_user_id,current_uid;
    String message;
    RecyclerView mRecycler;
    DatabaseReference mdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

        retrieve_shop_user_id = getIntent().getStringExtra("go_send_user_shop");

        mRecycler = findViewById(R.id.messages_list);

        mdata = FirebaseDatabase.getInstance().getReference().child("ChatMessages")
                .child(retrieve_shop_user_id)
                .child(current_uid);

        input_messages = findViewById(R.id.input_message);

        send = findViewById(R.id.send_message);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = input_messages.getText().toString();
                
                if (!TextUtils.isEmpty(message)){
                        sendMessages();
                }else {
                    Toast.makeText(chatMain.this, "Message is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        displayMessages();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser != null){

            current_uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        }
    }

    public void sendMessages(){
        Map mapMessages = new HashMap <>();
        mapMessages.put("messageText",message);
        mapMessages.put("messageTime", ServerValue.TIMESTAMP);
        FirebaseDatabase.getInstance().getReference().child("ChatMessages")
                .child(retrieve_shop_user_id)
                .child(current_uid).push()
                .setValue(mapMessages).addOnCompleteListener(new OnCompleteListener <Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(chatMain.this, "Message Successfully sent", Toast.LENGTH_SHORT).show();
                input_messages.setText(" ");
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signout,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.signOut){
            FirebaseAuth.getInstance().signOut();
        }
        return true;
    }

    public void displayMessages() {
        FirebaseRecyclerAdapter<ChatMessagesModel,MyViewHolder> fba = new FirebaseRecyclerAdapter <ChatMessagesModel, MyViewHolder>(
                ChatMessagesModel.class,
                R.layout.message_item_list,
                MyViewHolder.class,
                mdata
        ) {
            @Override
            protected void populateViewHolder(final MyViewHolder viewHolder, final ChatMessagesModel model, int position) {

            }

        };
        mRecycler.setAdapter(fba);
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        View mview;
        public MyViewHolder(View itemView) {
            super(itemView);
            mview = itemView;
        }

        public void setMes(String text){
            TextView msgText = mview.findViewById(R.id.receiver_message_layout);
            msgText.setText(text);
        }
    }
}
