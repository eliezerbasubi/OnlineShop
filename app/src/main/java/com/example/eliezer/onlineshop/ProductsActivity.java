package com.example.eliezer.onlineshop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class ProductsActivity extends AppCompatActivity {

    private DatabaseReference prodDatabase;
    private DatabaseReference mShopsDatabase;
    private DatabaseReference mUsersDatabase;
    private RecyclerView horizontalRecyclerview;
    private String current_user;
    private ImageView show_maps;
    private TextView set_details,shopPhoneNumber,shopEmailAddress;
    String getLongit,getLatit,get_shop_name,shoplogoLabel,fetch_shop_user;
    GPSTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);


        prodDatabase =  FirebaseDatabase.getInstance().getReference().child("Articles");
        prodDatabase.keepSynced(true);

        mShopsDatabase = FirebaseDatabase.getInstance().getReference().child("Shops");
        mShopsDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        current_user = getIntent().getStringExtra("product_details_list");
        fetch_shop_user = getIntent().getStringExtra("product_id");

        set_details = (TextView) findViewById(R.id.details_content);

        shopEmailAddress = findViewById(R.id.email_address_owner);

        shopPhoneNumber = findViewById(R.id.phone_number_owner);


        horizontalRecyclerview = (RecyclerView)findViewById(R.id.list_of_articles);
        horizontalRecyclerview.setHasFixedSize(true);
        horizontalRecyclerview.setLayoutManager(new LinearLayoutManager(ProductsActivity.this, LinearLayoutManager.HORIZONTAL,false));

        show_maps = (ImageView) findViewById(R.id.see_maps);


        //FETCH ABOUT SHOP DETAILS AND DISPLAY THEM
        final Query fetch_shop_details = mShopsDatabase.orderByChild("Shop_User").equalTo(current_user);

        fetch_shop_details.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    set_details.setText(data.child("Shop_Description").getValue().toString());
                    getLongit = data.child("Shop_Longitude" ).getValue().toString();
                    getLatit = data.child("Shop_Latitude").getValue().toString();
                    get_shop_name = data.child("Shop_Name").getValue().toString();
                    shoplogoLabel = data.child("Shop_Logo").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //Request internet access if not enabled
        try{

//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != MockPackageManager.PERMISSION_GRANTED){
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
//            }
        }catch (Exception e){
            e.printStackTrace();
        }


        show_maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                gpsTracker = new GPSTracker(ProductsActivity.this);
                if (gpsTracker.canGetLocation()){
                    double lat = gpsTracker.getLatitude();
                    double lon = gpsTracker.getLongitude();
                    String disp = lat +"  "+lon;

                    //POINT TO GOOGLE MAPS
                    //BEFORE CODE WAS POINTING TO GOOGLE PICKER
                    Intent placeIntent = new Intent(ProductsActivity.this,MapsActivity.class);
                    placeIntent.putExtra("longitude",getLongit);
                    placeIntent.putExtra("latitude",getLatit);
                    placeIntent.putExtra("shop_label",get_shop_name);
                    placeIntent.putExtra("shopLogo",shoplogoLabel);
                    placeIntent.putExtra("customer_latitude",String.valueOf(lat));
                    placeIntent.putExtra("customer_longitude",String.valueOf(lon));
                    startActivity(placeIntent);

                    Toast.makeText(ProductsActivity.this, disp, Toast.LENGTH_SHORT).show();
                }else {
                    gpsTracker.showSettingsAlert();
                }

            }
        });

        //get email and phone number
        EmailAndPhone();


    }

    @Override
    public void onBackPressed() {


        if (fetch_shop_user == null){
            //Toast.makeText(this, "Is null", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }else {
            Intent intent = new Intent(this,DetailsActivity.class);
            intent.putExtra("product_user", current_user);
            intent.putExtra("product_id", fetch_shop_user);
            //setResult(1, intent);
            startActivity(intent);
            //Toast.makeText(this, "Not null", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        final Query available_products = prodDatabase.orderByChild("shop_user").equalTo(current_user);

        FirebaseRecyclerAdapter<ArticleModel, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter <ArticleModel, ProductViewHolder>(
                ArticleModel.class,
                R.layout.details_shop_single_layout,
                ProductViewHolder.class,
                available_products
        ) {
            @Override
            protected void populateViewHolder(final ProductViewHolder viewHolder, final ArticleModel model, final int position) {
                available_products.addValueEventListener(new ValueEventListener() {
                    String product_id = getRef(position).getKey().toString();
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //FETCH ARTICLES DETAILS AND DISPLAY THEM
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                            viewHolder.bindName(model.getProduct_list());
                            viewHolder.bindPrice(model.getPrice_list());
                            viewHolder.bindImage(ProductsActivity.this, model.getImage_list());

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(ProductsActivity.this,DetailsActivity.class);
                                    intent.putExtra("product_id",product_id);
                                    intent.putExtra("product_user",model.shop_user);
                                    startActivity(intent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        horizontalRecyclerview.setAdapter(firebaseRecyclerAdapter);
    }

    public void EmailAndPhone(){
        //Go to users table and fetch email and phone number
        mUsersDatabase.child(current_user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                shopPhoneNumber.setText(dataSnapshot.child("Phone").getValue().toString());
                shopEmailAddress.setText(dataSnapshot.child("Email").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
         View mView;
        public ProductViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void bindName(String name){
            TextView bName = (TextView) mView.findViewById(R.id.prod_name_single);
            bName.setText(name);
        }

        public void bindPrice(String price){
            TextView bName = (TextView) mView.findViewById(R.id.prod_price_single);
            bName.setText(price);
        }

        public void bindImage(Context context,String image){
            ImageView bImage = (ImageView) mView.findViewById(R.id.prod_image_single);
            //Picasso.get().load(image).placeholder(R.drawable.lo).into(bImage);

            Glide
                    .with(context)
                    .load(image).apply(new RequestOptions()
                    .placeholder(R.drawable.lo)
                    .dontAnimate()
                    .dontTransform())
                    .into(bImage);
        }

    }
}
