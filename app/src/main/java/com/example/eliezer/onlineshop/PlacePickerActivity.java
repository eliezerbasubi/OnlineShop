package com.example.eliezer.onlineshop;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

public class PlacePickerActivity extends AppCompatActivity {

    int PICKER_FOR_PLACE = 1;
    private static int REQUEST_CODE_PERMISSION = 2;
    String permission = Manifest.permission.ACCESS_FINE_LOCATION;
    TextView showPlace, longitude_displayer ;

    GPSTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);

        showPlace = (TextView)findViewById(R.id.show_place);

        longitude_displayer = (TextView)findViewById(R.id.longitude_shower);

        //Check if permission is granted

    }

    public void goPlacePicker(View view){

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try{

            startActivityForResult(builder.build(PlacePickerActivity.this),PICKER_FOR_PLACE);
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

                Place place = PlacePicker.getPlace(PlacePickerActivity.this,data);
                showPlace.setText(place.getAddress());

                double getLat = place.getLatLng().latitude;

                double getLog = place.getLatLng().longitude;

                longitude_displayer.setText(getLat +"  "+getLog);

            }
        }
    }
}
