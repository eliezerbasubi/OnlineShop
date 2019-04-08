package com.example.eliezer.onlineshop;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShopFragment extends Fragment {

    private RecyclerView mShopView;
    private DatabaseReference mShopDatabase;
    private DatabaseReference mArticlesDatabase;
    private FirebaseAuth mAuth;
    String current_user;
    private TextView mResults;

    private View mMainView;

    public ShopFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView =  inflater.inflate(R.layout.fragment_shop, container, false);

        setHasOptionsMenu(true);

        //mShopView = mMainView.findViewById(R.id.shops_recylerview);

        mShopView = (RecyclerView) mMainView.findViewById(R.id.shops_recylerview);

        mResults = mMainView.findViewById(R.id.no_shop_found);

        // current_user = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        mShopDatabase = FirebaseDatabase.getInstance().getReference().child("Shops");
        mShopDatabase.keepSynced(true);

        mArticlesDatabase = FirebaseDatabase.getInstance().getReference().child("Articles");
        mArticlesDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        mShopView.setHasFixedSize(true);
        mShopView.setLayoutManager(new GridLayoutManager(getContext(),2));
        mShopView.setItemAnimator(new DefaultItemAnimator());
        mShopView.setNestedScrollingEnabled(false);
        return  mMainView;

    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser != null){
            current_user = mAuth.getCurrentUser().getUid().toString();
        }

        final Query available_products = mShopDatabase.orderByChild("status").equalTo("paid");

        FirebaseRecyclerAdapter<ShopsModel,ShopsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter <ShopsModel, ShopsViewHolder>(
                ShopsModel.class,
                R.layout.shops_single_layout,
                ShopsViewHolder.class,
                available_products
        ) {
            @Override
            protected void populateViewHolder(final ShopsViewHolder viewHolder, final ShopsModel model, int position) {
                String lid = getRef(position).getKey().toString();
                available_products.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //FETCH ARTICLES DETAILS AND DISPLAY THEM
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            final String shop_user_id = model.getShop_User();

                            String str = model.getShop_Name().toString();
                            String[] strArray = str.split(" ");
                            StringBuilder builder = new StringBuilder();
                            for (String s : strArray) {
                                String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                builder.append(cap + " ");
                            }
                            viewHolder.setShopName(builder.toString());
                           // viewHolder.setShopName(model.getShop_Name());
                            viewHolder.setShopPhysicalAddress(model.getShop_Address());
                            viewHolder.setterLogoImage(model.getShop_Logo(),getContext());

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent shopDetailsIntent = new Intent(getContext(), ProductsActivity.class);
                                    shopDetailsIntent.putExtra("product_details_list", shop_user_id);
                                    startActivity(shopDetailsIntent);


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

        mShopView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchViewShop = (SearchView)menuItem.getActionView();

        searchViewShop.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final Query shopQuery = mShopDatabase.orderByChild("Shop_Name").startAt(newText.toLowerCase()).endAt(newText.toLowerCase() + "\uf8ff");

                FirebaseRecyclerAdapter<ShopsModel,ShopsViewHolder> fireQueryAdapter = new FirebaseRecyclerAdapter <ShopsModel, ShopsViewHolder>(
                        ShopsModel.class,
                        R.layout.shops_single_layout,
                        ShopsViewHolder.class,
                        shopQuery
                ) {
                    @Override
                    protected void populateViewHolder(final ShopsViewHolder viewHolder, final ShopsModel model, int position) {
                        final String populate_id = getRef(position).getKey();

                        shopQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                //Check if the search input is not correct
                                if (dataSnapshot.getValue() == null){
                                    mResults.setVisibility(View.VISIBLE);
                                }
                                for (DataSnapshot dt : dataSnapshot.getChildren()){
                                    String str =model.getShop_Name();
                                    String[] strArray = str.split(" ");
                                    StringBuilder builder = new StringBuilder();
                                    for (String s : strArray) {
                                        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                        builder.append(cap + " ");
                                    }
                                    viewHolder.setShopName(builder.toString());
                                    viewHolder.setterLogoImage(model.getShop_Logo(),getContext());
                                    viewHolder.setShopPhysicalAddress(model.getShop_Address());

                                    final String shop_user_id = dt.child("Shop_User").getValue().toString();

                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent shopDetailsIntent = new Intent(getContext(),ProductsActivity.class);
                                            shopDetailsIntent.putExtra("product_details_list",shop_user_id);
                                            startActivity(shopDetailsIntent);

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
                mShopView.setAdapter(fireQueryAdapter);
                return true;
            }
        });
    }

    public static class ShopsViewHolder extends RecyclerView.ViewHolder{

        ImageView prodImage;
        View mView;
        ImageView moreDetails;
        public ShopsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            moreDetails = mView.findViewById(R.id.overflow_menu);
        }

        public void setShopName(String product_name) {
            TextView product = (TextView)mView.findViewById(R.id.shop_name_address);
            product.setText(product_name);
        }

        public void setShopCity(String shopCity) {
            TextView price = (TextView)mView.findViewById(R.id.shop_city_address);
            price.setText(shopCity);
        }

        public void setShopPhysicalAddress(String shopPhysicalAddress) {
            TextView price = (TextView)mView.findViewById(R.id.shop_physical_address);
            price.setText(shopPhysicalAddress);
        }


        public void setterLogoImage(String logo_thumb,Context context){
            prodImage = (ImageView)mView.findViewById(R.id.product_image);
            //Picasso.get().load(image_thumb).placeholder(R.drawable.lo).into(prodImage);

            Glide
                    .with(context)
                    .load(logo_thumb).apply(new RequestOptions()
                    .placeholder(R.drawable.lo)
                    .dontAnimate()
                    .dontTransform())
                    .into(prodImage);
        }
    }

}
