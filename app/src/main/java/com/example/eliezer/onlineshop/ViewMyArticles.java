package com.example.eliezer.onlineshop;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ViewMyArticles extends AppCompatActivity {

    private RecyclerView mProductView;
    private DatabaseReference mProductDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    String current_user,value_product,value_price;
    private CardView cardView;
    private Button cancel, update;
    private EditText article_name, article_price;
    private TextView found_article;
    private ProgressBar progressBar;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_articles);

        mProductView = (RecyclerView)findViewById(R.id.recycler_list_adapter);

        cardView = findViewById(R.id.card);
        found_article = findViewById(R.id.no_article_found);

        mToolbar = findViewById(R.id.app_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //progressBar = findViewById(R.id.progressUpdate);



        current_user = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        mProductView = findViewById(R.id.viewRecycler);

        mProductDatabase = FirebaseDatabase.getInstance().getReference().child("Articles");
        mProductDatabase.keepSynced(true);

        mProductView.setHasFixedSize(true);
        mProductView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mProductView.setItemAnimator(new DefaultItemAnimator());
        mProductView.setNestedScrollingEnabled(false);
    }

    @Override
    public void onStart() {
        super.onStart();

        final Query queryArticles = mProductDatabase.orderByChild("shop_user").equalTo(current_user);

        FirebaseRecyclerAdapter<ArticleModel,HomeViewHolder> firebaseAdapter = new FirebaseRecyclerAdapter<ArticleModel, HomeViewHolder>(
                ArticleModel.class,
                R.layout.view_articles_layout,
                HomeViewHolder.class,
                queryArticles
        ) {
            @Override
            protected void populateViewHolder(final HomeViewHolder viewHolder, final ArticleModel model, int position) {
                final String list_product_id = getRef(position).getKey();

                queryArticles.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            found_article.setVisibility(View.GONE);
                        }

                        String str =model.getProduct_list();
                        String[] strArray = str.split(" ");
                        StringBuilder builder = new StringBuilder();
                        for (String s : strArray) {
                            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                            builder.append(cap + " ");
                        }
                        viewHolder.setProductName(builder.toString());
                        // viewHolder.setProductName(product_name);
                        viewHolder.setPrice(model.price_list);
                        viewHolder.setterImage(model.image_list,getApplicationContext());

                        //HANDLE BUTTTONS
                        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                createDialog(list_product_id,model.getProduct_list());

                                /**/
                            }
                        });

                        android.app.AlertDialog.Builder builderUpdate = new android.app.AlertDialog.Builder(ViewMyArticles.this);
                        final View viewUpdate = LayoutInflater.from(getApplicationContext()).inflate(R.layout.update_article_layout,null);
                       builderUpdate.setView(viewUpdate);

                        cancel = viewUpdate.findViewById(R.id.cancel_update);
                        update = viewUpdate.findViewById(R.id.official_update);
                        article_name = viewUpdate.findViewById(R.id.new_article_name);
                        article_price = viewUpdate.findViewById(R.id.new_article_price);
                        progressBar = viewUpdate.findViewById(R.id.progressUpdate);

                        final android.app.AlertDialog dialog = builderUpdate.create();

                        //Update button handler
                        viewHolder.update.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //Set values for price and product name
                                article_price.setText(model.getPrice_list());
                                article_name.setText(model.getProduct_list());

                                //Get values of price and product
                                value_product = article_name.getText().toString();
                                value_price = article_price.getText().toString();


                                //When cancel button is clicked, return true

                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        return;
                                    }
                                });

                                //Update article name or price when button clicked
                                update.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (article_price.getText().toString().isEmpty()){
                                            article_price.requestFocus();
                                            article_price.setError("Required");
                                            progressBar.setVisibility(View.GONE);
                                        }

                                        if (article_name.getText().toString().isEmpty()){
                                            article_name.requestFocus();
                                            article_name.setError("Required");
                                            progressBar.setVisibility(View.GONE);
                                        }

                                        if (!article_name.getText().toString().isEmpty() && !article_price.getText().toString().isEmpty()) {
                                            HashMap <String, String> mapArticle = new HashMap <>();
                                            mapArticle.put("product_list", value_product.toLowerCase());
                                            mapArticle.put("price_list", value_price);


                                            progressBar.setVisibility(View.VISIBLE);

                                            //Update product name first
                                            if (!model.getProduct_list().equals(article_name.getText().toString().toLowerCase())) {
                                                mProductDatabase.child(list_product_id).child("product_list").setValue(article_name.getText().toString().toLowerCase()).addOnCompleteListener(new OnCompleteListener <Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task <Void> task) {
                                                        if (task.isSuccessful()) {
                                                            progressBar.setVisibility(View.GONE);
                                                            dialog.dismiss();
                                                            Toast.makeText(ViewMyArticles.this, value_product + "successfully updated", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Snackbar.make(cardView, "Unable to delete " + model.getPrice_list(), Snackbar.LENGTH_LONG).setAction("Try Again", new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    return;
                                                                }
                                                            }).show();
                                                        }
                                                    }
                                                });

                                            }//end if product equals model product

                                            //Update product price
                                            mProductDatabase.child(list_product_id).child("price_list").setValue(article_price.getText().toString()).addOnCompleteListener(new OnCompleteListener <Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task <Void> task) {
                                                    if (task.isSuccessful()){
                                                        progressBar.setVisibility(View.GONE);
                                                        dialog.dismiss();
                                                        Toast.makeText(ViewMyArticles.this, "price successfully updated", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Snackbar.make(cardView,"Unable to delete "+model.getPrice_list(),Snackbar.LENGTH_LONG).setAction("Try Again", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                return;
                                                            }
                                                        }).show();
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });

                                dialog.show();
                            }
                        });
                    }//End of onDataChange method

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };


        mProductView.setAdapter(firebaseAdapter);
    }

    public void createDialog(final String link, final String product_name){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Delete Article");
        alertBuilder.setMessage("Are you sure to delete this article");
        alertBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mProductDatabase.child(link).removeValue().addOnCompleteListener(new OnCompleteListener <Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(ViewMyArticles.this, product_name +"Successfully removed", Toast.LENGTH_SHORT).show();
                        }else{
                            Snackbar.make(cardView,"Unable to delete "+product_name,Snackbar.LENGTH_LONG).setAction("Try Again", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    return;
                                }
                            }).show();
                        }
                    }
                });
            }
        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        AlertDialog dialogBuilder = alertBuilder.create();
        dialogBuilder.show();
    }

    public static class HomeViewHolder extends RecyclerView.ViewHolder{

        ImageView prodImage;
        View mView;
        ImageView moreDetails;
        Button delete,update;
        public HomeViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            delete = mView.findViewById(R.id.delete_article);
            update = mView.findViewById(R.id.update_article);
        }

        public void setProductName(String product_name) {
            TextView product = (TextView)mView.findViewById(R.id.view_article_name);
            product.setText(product_name);
        }

        public void setPrice(String product_price) {
            TextView price = (TextView)mView.findViewById(R.id.view_price);
            price.setText(product_price);
        }

        public void setterImage(String image_thumb,Context context){
            prodImage = (ImageView)mView.findViewById(R.id.view_circleImageView);
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
