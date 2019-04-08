package com.example.eliezer.onlineshop;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.internal.NavigationMenu;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.yavski.fabspeeddial.FabSpeedDial;

public class DetailsActivity extends AppCompatActivity {

    private ImageView details_image;
    private TextView details_prod_title, details_prod_price,details_content;
    private Toolbar toolbar;
    String setToolTitle;
    private FabSpeedDial fabSpeedDial;
    String extract_user, extract_phone;
    String product_id, from_product_name,product_user;
    private String shop_uid;
    RecyclerView mRecylerview;
    List<String> listShopIDs;


    private DatabaseReference mDatabaseReference;
    private DatabaseReference mPhoneDatabase;
    private DatabaseReference mShopsDatabase;
    private SharedPreferences setKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);


        from_product_name = getIntent().getStringExtra("product_name");

        toolbar = findViewById(R.id.toolbar_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(from_product_name);
        final ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        details_image = (ImageView) findViewById(R.id.details_product_image);
        details_prod_title = (TextView) findViewById(R.id.details_product_title);
        details_prod_price = (TextView) findViewById(R.id.details_product_price);
        details_content = (TextView) findViewById(R.id.details_content);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Articles");
        mDatabaseReference.keepSynced(true);

        mPhoneDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mPhoneDatabase.keepSynced(true);

        mShopsDatabase = FirebaseDatabase.getInstance().getReference().child("Shops");
        mShopsDatabase.keepSynced(true);


        product_id = getIntent().getStringExtra("product_id");
        product_user = getIntent().getStringExtra("product_user");


        //Retrieve product id
        retrieveProduct();


        mRecylerview = findViewById(R.id.available_recyclerview);
        mRecylerview.setHasFixedSize(true);
        mRecylerview.setLayoutManager(new LinearLayoutManager(this));


        //FAB MENU

        fabSpeedDial = findViewById(R.id.fabMenu);
        fabSpeedDial.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.call:
                        getPno();
                        return true;
                    /*case R.id.message_pop:
                        Intent chatIntent = new Intent(DetailsActivity.this, SignUpChatActivity.class);
                        chatIntent.putExtra("chat_shop_user_id",product_user);
                        startActivity(chatIntent);

                       // Toast.makeText(DetailsActivity.this, product_user, Toast.LENGTH_SHORT).show();
                        return true;*/
                    case R.id.email_pop:
                        Intent toEmailIntent = new Intent(DetailsActivity.this, SendEmailActivity.class);
                        toEmailIntent.putExtra("extract_email_of_user", extract_user);
                        toEmailIntent.putExtra("product_id", product_id);
                        toEmailIntent.putExtra("product_user", product_user);
                        toEmailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(toEmailIntent);

                        //Toast.makeText(DetailsActivity.this, "Email shop owner", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public void onMenuClosed() {

            }
        });


    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                product_user = getIntent().getStringExtra("product_user");
                Toast.makeText(this, product_user, Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //get available shops with same products
        Query query = mDatabaseReference.orderByChild("product_list").equalTo(from_product_name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dst : dataSnapshot.getChildren()){
                    shop_uid = dst.child("shop_user").getValue().toString();
                    //Toast.makeText(DetailsActivity.this, shop_uid, Toast.LENGTH_SHORT).show();
                }
                //To save
                /*SharedPreferences settings = getSharedPreferences("YOUR_PREF_NAME", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("SNOW_DENSITY",shop_uid);
                editor.commit();*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*SharedPreferences settings = getSharedPreferences("YOUR_PREF_NAME", 0);
        String snowDensity = settings.getString("SNOW_DENSITY", "shut up");
        Toast.makeText(DetailsActivity.this, snowDensity, Toast.LENGTH_SHORT).show();*/

        //Go into shops node and fetch all the shops with same product
        final Query getAvail = mShopsDatabase.orderByChild("Shop_User").equalTo(product_user);

        FirebaseRecyclerAdapter <ShopsModel, AvailableViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter <ShopsModel, AvailableViewHolder>(
                ShopsModel.class,
                R.layout.available_shop_single_layout,
                AvailableViewHolder.class,
                getAvail
        ) {
            @Override
            protected void populateViewHolder(final AvailableViewHolder viewHolder, final ShopsModel model, int position) {
                getAvail.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                            viewHolder.nameAndLocation(model.getShop_Name(), model.getShop_Address());
                            viewHolder.sImage(DetailsActivity.this, model.getShop_Logo());

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent details_shops = new Intent(DetailsActivity.this,ProductsActivity.class);
                                    details_shops.putExtra("product_details_list",model.getShop_User());
                                    details_shops.putExtra("product_id",product_id);
                                    startActivityForResult(details_shops,1);
                                }
                            });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mRecylerview.setAdapter(firebaseRecyclerAdapter);
    }



    public  void getPno(){
        mPhoneDatabase.child(extract_user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               // Toast.makeText(DetailsActivity.this, extract_user, Toast.LENGTH_SHORT).show();

                 extract_phone = dataSnapshot.child("Phone").getValue().toString();

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DetailsActivity.this);

                alertDialog.setTitle("Make Call");

                alertDialog.setMessage("Do you want to call this number ? "+ Html.fromHtml("<b>"+extract_phone+"</b>"));
                alertDialog.setPositiveButton("Call", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent call = new Intent(Intent.ACTION_CALL);
                        call.setData(Uri.parse("tel:"+extract_phone));


                        if (ActivityCompat.checkSelfPermission(DetailsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(DetailsActivity.this, "Please grant permission", Toast.LENGTH_SHORT).show();
                            ActivityCompat.requestPermissions(DetailsActivity.this,new String []{Manifest.permission.CALL_PHONE},1);
                        }else {
                            startActivity(call);
                        }

                    }
                }) ;
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void retrieveProduct(){

        mDatabaseReference.child(product_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                details_prod_price.setText(dataSnapshot.child("price_list").getValue().toString());
                details_prod_title.setText(dataSnapshot.child("product_list").getValue().toString());
                details_content.setText(dataSnapshot.child("description_list").getValue().toString());
                extract_user = dataSnapshot.child("shop_user").getValue().toString();
                //Picasso.get().load(dataSnapshot.child("image_list").getValue().toString()).placeholder(R.drawable.lo).into(details_image);

                //Toast.makeText(DetailsActivity.this,extract_user, Toast.LENGTH_SHORT).show();

                Glide.with(DetailsActivity.this).load(dataSnapshot.child("image_list").getValue().toString())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.lo)
                                .dontAnimate()
                                .dontTransform())
                        .into(details_image);

                setToolTitle = dataSnapshot.child("product_list").getValue().toString();

                getSupportActionBar().setTitle(setToolTitle);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static class AvailableViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public AvailableViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void nameAndLocation(String name,String location){
            TextView sname = mView.findViewById(R.id.available_shop_name);
            sname.setText(name);

            TextView slocation = mView.findViewById(R.id.available_shop_location);
            slocation.setText(location);
        }

        public void sImage(Context ctx,String url){
            CircleImageView cImage = mView.findViewById(R.id.available_circleImageView);
            Glide
                    .with(ctx)
                    .load(url).apply(new RequestOptions()
                    .placeholder(R.drawable.lo)
                    .dontAnimate()
                    .dontTransform())
                    .into(cImage);
        }
    }
}
