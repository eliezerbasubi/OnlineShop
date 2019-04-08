package com.example.eliezer.onlineshop;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class ArticleActivity extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle actionDrawer;

    private TextInputEditText prod_name,price,description;
    private Button btnConf,paypalBtn;
    private ImageButton imageButton;
    private ImageView article_image,shop_logo;
   // private Spinner categories;
    private StorageReference mStorageRef;
    FirebaseDatabase database;
    FirebaseUser user;
    DatabaseReference articleDatabase;
    DatabaseReference mRootRef;
    DatabaseReference logoRef;
    DatabaseReference userDatabase;

    FirebaseUser mChatUser;

    String mUser;
    Uri imageUri;
    Uri resultUri;
    private byte[] thumb_byte = new byte[0];
    private File thumb_filePath;

    String push_generator;


    private ProgressDialog progressDialog;

    NavigationView navigationView;


    private static final int PICK_IMAGE_REQUEST  = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.shop_menu);

        mDrawer = (DrawerLayout)findViewById(R.id.drawer);
       // actionDrawer = new ActionBarDrawerToggle(this,mDrawer,R.string.open,R.string.close);

        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawer.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

      //  mDrawer.addDrawerListener(actionDrawer);
       // actionDrawer.syncState();
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(navigationView);




        //Firebase handling part

        mStorageRef = FirebaseStorage.getInstance().getReference();

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();

        articleDatabase =database.getReference().child("Articles");
        articleDatabase.keepSynced(true);

        logoRef =database.getReference().child("Shops");
        logoRef.keepSynced(true);

        userDatabase =database.getReference().child("Users");
        userDatabase.keepSynced(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

//        uid = user.getUid();


        mChatUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser  = mChatUser.getUid();



        //GET PUSH ID FOR THE ROOT
        push_generator = mRootRef.push().getKey().toString();


        prod_name=findViewById(R.id.name);
        price=findViewById(R.id.price);
        description=findViewById(R.id.description);
        btnConf=findViewById(R.id.btnConf);
        imageButton=findViewById(R.id.image_selector);
        article_image = findViewById(R.id.article_image);


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), PICK_IMAGE_REQUEST);*/

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ArticleActivity.this);
            }
        });


        btnConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String product_name= prod_name.getText().toString();
                String product_price= price.getText().toString();
                String product_desc= description.getText().toString();

                //Add_article(product_name,product_price);

               // UploadImages(product_name,product_price,product_desc);
                VerifyPayment(product_name,product_price,product_desc);

            }
        });


        /* ADD PROFILE IMAGE OF CURRENT SHOP */
        addShopLogo();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);

            thumb_filePath = new File(imageUri.getPath());

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();

                thumb_filePath = new File(resultUri.getPath());

                //IF CROPPING IT'S OKAY, IT SHOULD DISPLAY THE IMAGE


                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    article_image.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                thumb_byte = baos.toByteArray();

            }
        }

    }


    public void VerifyPayment(final String product_name, final String product_price, final String product_description){
        final Query payment_query = logoRef.orderByChild("Shop_User").equalTo(mUser);
        payment_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String verify_status = data.child("status").getValue().toString();

                    if (verify_status.equals("paid")){
                        UploadImages(product_name,product_price,product_description);
                    }

                    if (verify_status.equals("unpaid")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(ArticleActivity.this);
                        final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.payment_layout,null);
                        builder.setView(view);

                        final AlertDialog dialog = builder.create();

                        paypalBtn = view.findViewById(R.id.paypal_button);
                        paypalBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                               startActivity(new Intent(ArticleActivity.this,Payment.class));
                            }
                        });

                        dialog.show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void UploadImages(final String product_name, final String product_price, final String product_description){


        DatabaseReference databaseReference= mRootRef.child("Articles").push();

        final String push_id = databaseReference.getKey();

        //final String current_user_ref = "Articles" +"/"+ uid;



        final StorageReference riversRef = mStorageRef.child("shop_images").child(push_id+".jpg");

        final StorageReference thumb_storage = mStorageRef.child("shop_images").child("thumbs").child(push_id+".jpg");


        //CHECK IF PRODUCT NAME AND PRICE ARE EMPTY TO AVOID USER FROM STORING UNWANTED IMAGES

        if (!TextUtils.isEmpty(prod_name.getText()) || !TextUtils.isEmpty(price.getText())|| !TextUtils.isEmpty(description.getText())) {


            progressDialog = new ProgressDialog(ArticleActivity.this);
            progressDialog.setTitle("Upload Articles");
            progressDialog.setMessage("Wait until the uploading process is done");
            progressDialog.show();


            final String uid = user.getUid().toString();

            if (resultUri != null) {
                riversRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener <UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task <UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_storage.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener <UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task <UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();


                                    if (thumb_task.isSuccessful()) {
                                        Map shopMap = new HashMap <>();
                                        shopMap.put("Image", thumb_downloadUrl);

                                        final HashMap <String, String> shops = new HashMap <>();
                                        shops.put("product_list", product_name.toLowerCase());
                                        shops.put("price_list", product_price);
                                        shops.put("description_list", product_description);
                                        shops.put("image_list", thumb_downloadUrl);
                                        shops.put("shop_user", uid);

                                        DatabaseReference databaseReference = mRootRef.push();

                                        String push_id = databaseReference.getKey();

                                        //CHECK IF USER HAS ADDED 5 ARTICLES

                                        Query numberArticles = articleDatabase.orderByChild("shop_user").equalTo(uid);

                                        numberArticles.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                    Long num_articles = dataSnapshot.getChildrenCount();
                                                    if (num_articles <= 4) {
                                                        articleDatabase.push().setValue(shops).addOnCompleteListener(new OnCompleteListener <Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task <Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    progressDialog.dismiss();

                                                                    ClearFields();

                                                                    Toast.makeText(ArticleActivity.this, "Article successfully added", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }else {
                                                        //Toast.makeText(ArticleActivity.this, "You've reached the maximum, Please pay for more", Toast.LENGTH_SHORT).show();
                                                        Snackbar snackBar = Snackbar.make(mDrawer, "You've reached the maximum, Pay for more articles", Snackbar.LENGTH_LONG)
                                                                .setAction("Retry", new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        return ;
                                                                    }
                                                                });
                                                        snackBar.show();

                                                        ClearFields();

                                                        progressDialog.dismiss();
                                                    }
                                               
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    } else {
                                        Toast.makeText(ArticleActivity.this, "Image thumbnail failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    }
                });
            }else {
                Toast.makeText(ArticleActivity.this, "Uri is Empty, Please insert an image", Toast.LENGTH_SHORT).show();
            }
        }//End of if product name and product price are not empty

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;

            case R.id.nav_gallery:
                Intent settingsIntent = new Intent(ArticleActivity.this,MainHome.class);
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(settingsIntent);
                mDrawer.openDrawer(GravityCompat.START);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }



    //handle navigation view on item click
    public void selectItemDrawer(MenuItem menuItem){

        switch (menuItem.getItemId()){
            case R.id.nav_gallery:
                Intent homeIntent = new Intent(ArticleActivity.this,MainHome.class);
                startActivity(homeIntent);
                break;

            case R.id.nav_logout :
                /*FirebaseAuth.getInstance().signOut();
                Intent startIntent = new Intent(ArticleActivity.this,LoginActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(startIntent);
                finish();*/
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.eliezer.admin");
                if (launchIntent != null) {
                    startActivity(launchIntent);//null pointer check in case package name was not found
                }
                break;

            case R.id.myArticles :
                startActivity(new Intent(ArticleActivity.this,ViewMyArticles.class));
                break;

        }

    }

    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectItemDrawer(item);
                return false;
            }
        });
    }

    public void ClearFields(){
        prod_name.setText("");
        description.setText("");
        price.setText("");
        article_image.setImageBitmap(null);
    }

    public void addShopLogo(){
       mUser = user.getUid().toString();
        View view = navigationView.getHeaderView(0);

        shop_logo = view.findViewById(R.id.shop_logo_here);
        final TextView textView = view.findViewById(R.id.example);
        final TextView txtEmail = view.findViewById(R.id.email_example);
        final TextView txtNumber = view.findViewById(R.id.number_example);


       final Query query = logoRef.orderByChild("Shop_User").equalTo(mUser);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Glide.with(ArticleActivity.this).load(data.child("Shop_Logo").getValue().toString())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.lo)
                                    .dontAnimate()
                                    .dontTransform())
                            .into(shop_logo);

                    String str = data.child("Shop_Name").getValue().toString();
                    String[] strArray = str.split(" ");
                    StringBuilder builder = new StringBuilder();
                    for (String s : strArray) {
                        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                        builder.append(cap + " ");
                    }

                    //Comment name of the shop
                    //textView.setText(builder.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Retrieve information about shop user.
        //details like name, email and phone number

        userDatabase.child(mUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                textView.setText(dataSnapshot.child("Firstname").getValue().toString());
                txtNumber.setText(dataSnapshot.child("Phone").getValue().toString());
                txtEmail.setText(dataSnapshot.child("Email").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void onBackPressed() {
        DrawerLayout layout = (DrawerLayout)findViewById(R.id.drawer);
        if (layout.isDrawerOpen(GravityCompat.START)) {
            layout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
