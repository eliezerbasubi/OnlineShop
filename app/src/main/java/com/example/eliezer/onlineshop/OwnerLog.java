package com.example.eliezer.onlineshop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class OwnerLog extends AppCompatActivity {

    Button proceed;
    FirebaseDatabase database;
    StorageReference mStorageDatabase;
    FirebaseUser user;
    FirebaseAuth auth;
    DatabaseReference ref;
    String current_user;
    TextInputEditText shopName,shopDescription;
    TextView locationViewer, altitudeViewer;
    Toolbar mToolbar;
    ImageView logoImage;

    ProgressDialog mProgress;

    String getShopLocation;

    double getLat,getLog;
    int PICKER_FOR_PLACE =1;
    int PICK_IMAGE_REQUEST = 2;

    Uri imageUri;
    Uri resultUri;
    private byte[] thumb_byte = new byte[0];
    private File thumb_filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_log);

        mToolbar = (Toolbar)findViewById(R.id.owner_log_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Shop Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        logoImage = (ImageView)findViewById(R.id.shop_logo);

        shopName =(TextInputEditText)findViewById(R.id.shop_name_here);

        shopDescription =(TextInputEditText)findViewById(R.id.description_here);

        locationViewer = (TextView)findViewById(R.id.location_viewer_current);

        altitudeViewer = (TextView)findViewById(R.id.lonLat_current);

        user = FirebaseAuth.getInstance().getCurrentUser();

        proceed=findViewById(R.id.proceed);

        database = FirebaseDatabase.getInstance();
        mStorageDatabase = FirebaseStorage.getInstance().getReference();
        ref = database.getReference().child("Shops");

        auth = FirebaseAuth.getInstance();

         current_user = auth.getCurrentUser().getUid();


        //getting data


        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shopnames = shopName.getEditableText().toString();

                String shopaddres = shopDescription.getEditableText().toString();

                Intent intent=new Intent(OwnerLog.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

               //Initialize progress dialog
                mProgress = new ProgressDialog(OwnerLog.this);
                mProgress.setTitle("Creating Shop");
                mProgress.setMessage("Wait until your shop is successfully created...");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                if(shopnames.isEmpty()){
                    shopName.setError("required");
                    shopName.requestFocus();
                    return;

                }

                if(shopaddres.isEmpty()){
                    shopDescription.setError("required");
                    shopDescription.requestFocus();
                    return;

                }

                //Send shop details to login
                //Make sure user has clicked on email link
                //Comment first this method and call it in login check email verification method

                if (altitudeViewer.getText().length() >0 || locationViewer.getText().length() >0) {
                    createShop(shopnames, shopaddres);
                }else {
                    Toast.makeText(OwnerLog.this, "OnlineShop cannot get your location", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void fileChooser(View view){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(4,4)
                .start(OwnerLog.this);
    }

    public void goToPlacePicker(View view){

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try{

            startActivityForResult(builder.build(OwnerLog.this),PICKER_FOR_PLACE);
        }catch (GooglePlayServicesRepairableException e){
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKER_FOR_PLACE){
            if (resultCode == RESULT_OK){

                Place place = PlacePicker.getPlace(OwnerLog.this,data);
                locationViewer.setText(place.getAddress());

                getShopLocation = place.getAddress().toString();

                getLat = place.getLatLng().latitude;

                getLog = place.getLatLng().longitude;

                altitudeViewer.setText(getLat +"  "+getLog);

            }
        }

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
                    logoImage.setImageBitmap(bitmap);

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

    public void createShop(final String name, final String country){

        final String push_id = ref.getKey();

        //final String current_user_ref = "Articles" +"/"+ uid;



        final StorageReference riversRef = mStorageDatabase.child("shop_images").child(ServerValue.TIMESTAMP+".jpg");

        final StorageReference thumb_storage = mStorageDatabase.child("shop_images").child("thumbs").child(ServerValue.TIMESTAMP+".jpg");


        if (resultUri != null ){
            riversRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener <UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task <UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        final String download_url = task.getResult().getDownloadUrl().toString();

                        UploadTask uploadTask = thumb_storage.putBytes(thumb_byte);

                        uploadTask.addOnCompleteListener(new OnCompleteListener <UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task <UploadTask.TaskSnapshot> nail_task) {
                                String thumb_downloadUrl = nail_task.getResult().getDownloadUrl().toString();
                                    if (nail_task.isSuccessful()){

                        //Check if user has picker the location of his shop
                                        if(current_user !=null){
                                            final HashMap<String,String> shops = new HashMap<>();
                                            shops.put("Shop_Name",name.toLowerCase());
                                            shops.put("Shop_Description",country);
                                            shops.put("Shop_Logo",thumb_downloadUrl);
                                            shops.put("Shop_Address",getShopLocation);
                                            shops.put("Shop_Longitude",String.valueOf(getLog));
                                            shops.put("Shop_Latitude",String.valueOf(getLat));
                                            shops.put("Shop_User",current_user);
                                            shops.put("status","unpaid");

                                            ref.push().setValue(shops).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        mProgress.dismiss();
                                                        Intent intent=new Intent(OwnerLog.this,LoginActivity.class);
                                                        //intent.putExtra("name_id",name);
                                                        //intent.putExtra("description_id",country);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });

                                        }


                                    }
                            }
                        });
                    }
                }
            });
        }else {
            Toast.makeText(OwnerLog.this, "Invalid image uri", Toast.LENGTH_SHORT).show();
        }

    }
}