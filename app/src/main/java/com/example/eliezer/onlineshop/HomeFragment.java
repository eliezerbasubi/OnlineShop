package com.example.eliezer.onlineshop;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import static com.google.firebase.database.FirebaseDatabase.getInstance;

public class HomeFragment extends Fragment {

    private RecyclerView mProductView;
    private DatabaseReference mProductDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    String current_user;
    String overflowSearch;

    private View mMainView;

    private TextView resultFound;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        mMainView = inflater.inflate(R.layout.fragment_home, container, false);

        mProductView = (RecyclerView) mMainView.findViewById(R.id.recycler_list_adapter);

        resultFound =  mMainView.findViewById(R.id.result_found);

       // current_user = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        mProductDatabase = FirebaseDatabase.getInstance().getReference().child("Articles");
        mProductDatabase.keepSynced(true);

        mProductView.setHasFixedSize(true);
        mProductView.setLayoutManager(new GridLayoutManager(getContext(),2));
        mProductView.setItemAnimator(new DefaultItemAnimator());
        mProductView.setNestedScrollingEnabled(false);
        return  mMainView;

    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<ArticleModel,HomeViewHolder> firebaseAdapter = new FirebaseRecyclerAdapter<ArticleModel, HomeViewHolder>(
                ArticleModel.class,
                R.layout.products_single_layout,
                HomeViewHolder.class,
                mProductDatabase
        ) {
            @Override
            protected void populateViewHolder(final HomeViewHolder viewHolder, ArticleModel model, int position) {
                final String list_product_id = getRef(position).getKey();

                mProductDatabase.child(list_product_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String product_name = dataSnapshot.child("product_list").getValue().toString();
                        String product_price = dataSnapshot.child("price_list").getValue().toString();
                        String product_image = dataSnapshot.child("image_list").getValue().toString();
                        final String product_user = dataSnapshot.child("shop_user").getValue().toString();


                        String str =product_name;
                        String[] strArray = str.split(" ");
                        StringBuilder builder = new StringBuilder();
                        for (String s : strArray) {
                            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                            builder.append(cap + " ");
                        }
                        viewHolder.setProductName(builder.toString());
                        // viewHolder.setProductName(product_name);
                        viewHolder.setPrice(product_price);
                        viewHolder.setterImage(product_image,getContext());


                        viewHolder.moreDetails.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PopupMenu popup = new PopupMenu(getContext(), view);
                                popup.getMenuInflater().inflate(R.menu.shop_item_menu, popup.getMenu());
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    public boolean onMenuItemClick(MenuItem item) {
                                        Log.i("TEST",item.getTitle().toString());
                                        Intent detailsIntent = new Intent(getContext(),DetailsActivity.class);
                                        detailsIntent.putExtra("product_id",list_product_id);
                                        detailsIntent.putExtra("product_name",product_name);
                                        detailsIntent.putExtra("product_user",product_user);
                                        startActivity(detailsIntent);
                                        return true;
                                    }
                                });
                                popup.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mProductView.setAdapter(firebaseAdapter);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //newText = searchView.getQuery().toString();


                if(newText.isEmpty()){
                    //Toast.makeText(getContext(), "No search", Toast.LENGTH_SHORT).show();
                   onStart();
                }else {
                    searchItemHome(newText.toLowerCase());
                    //Toast.makeText(getContext(), newText.toLowerCase(), Toast.LENGTH_SHORT).show();
                }

                return true;
            }


        });
    }



    public void searchItemHome(final String query){

        final Query itemQuery = mProductDatabase.orderByChild("product_list").startAt(query).endAt(query + "\uf8ff");

        FirebaseRecyclerAdapter<ArticleModel,HomeViewHolder> firebaseAdapter = new FirebaseRecyclerAdapter<ArticleModel, HomeViewHolder>(
                ArticleModel.class,
                R.layout.products_single_layout,
                HomeViewHolder.class,
                itemQuery
        ) {
            @Override
            protected void populateViewHolder(final HomeViewHolder viewHolder, final ArticleModel model, int position) {
                final String list_product_id = getRef(position).getKey();

                itemQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()){

                            String str =model.getProduct_list();
                            String[] strArray = str.split(" ");
                            StringBuilder builder = new StringBuilder();
                            for (String s : strArray) {
                                String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                builder.append(cap + " ");
                            }
                            viewHolder.setProductName(builder.toString());
                            viewHolder.setPrice(model.getPrice_list());
                            viewHolder.setterImage(model.getImage_list(),getContext());

                            final String prodName = data.child("product_list").getValue().toString();
                            final String product_user = data.child("shop_user").getValue().toString();

                            viewHolder.moreDetails.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    PopupMenu popup = new PopupMenu(getContext(), view);
                                    popup.getMenuInflater().inflate(R.menu.shop_item_menu, popup.getMenu());
                                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        public boolean onMenuItemClick(MenuItem item) {
                                            Log.i("TEST",item.getTitle().toString());
                                            Intent detailsIntent = new Intent(getContext(),DetailsActivity.class);
                                            detailsIntent.putExtra("product_id",list_product_id);
                                            detailsIntent.putExtra("product_name",prodName);
                                            detailsIntent.putExtra("product_user",product_user);
                                            startActivity(detailsIntent);

                                            return true;
                                        }
                                    });
                                    popup.show();
                                }
                            });

                            //Check if the search query exists
                            /*if (data.hasChild(query)){
                                Toast.makeText(getContext(), "Not found", Toast.LENGTH_SHORT).show();
                            }*/
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("QUERY ERROR",databaseError.getMessage());
                    }
                });

            }
        };

        mProductView.setAdapter(firebaseAdapter);
    }






    public static class HomeViewHolder extends RecyclerView.ViewHolder{

        ImageView prodImage;
        View mView;
        ImageView moreDetails;
        public HomeViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            moreDetails = mView.findViewById(R.id.overflow_menu);
        }

        public void setProductName(String product_name) {
            TextView product = (TextView)mView.findViewById(R.id.product_name);
            product.setText(product_name);
        }

        public void setPrice(String product_price) {
            TextView price = (TextView)mView.findViewById(R.id.product_price);
            price.setText(product_price);
        }

        public void setterImage(String image_thumb,Context context){
            prodImage = (ImageView)mView.findViewById(R.id.product_image);
            //Picasso.get().load(image_thumb).placeholder(R.drawable.lo).into(prodImage);

            Glide
                    .with(context)
                    .load(image_thumb).apply(new RequestOptions()
                    .placeholder(R.drawable.lo)
                    .dontAnimate()
                    .dontTransform())
                    .into(prodImage);
        }
    }


}
