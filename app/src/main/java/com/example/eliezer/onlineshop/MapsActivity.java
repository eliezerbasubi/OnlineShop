package com.example.eliezer.onlineshop;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener,GoogleMap.OnMarkerClickListener,RoutingListener {

    private GoogleMap mMap;
    private double receive_longitude,costumer_receiver_longitude;
    private double receive_latitude,costumer_receiver_latitude;
    String converter_lat, converter_long,convert_shop_label,convert_logo,costumer_conv_lat,costumer_conv_long;

    TextView distanceText;

    float distance;
    LatLng customer;

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorMaroon,R.color.colorPrimaryDark,R.color.primary_dark_material_light};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        converter_long = getIntent().getExtras().getString("longitude");
        converter_lat = getIntent().getExtras().getString("latitude");
        convert_shop_label = getIntent().getExtras().getString("shop_label");
        convert_logo = getIntent().getExtras().getString("shopLogo");
        costumer_conv_lat = getIntent().getExtras().getString("customer_latitude");
        costumer_conv_long = getIntent().getExtras().getString("customer_longitude");

        receive_longitude = Double.parseDouble(converter_long);
        receive_latitude = Double.parseDouble(converter_lat);
        costumer_receiver_latitude = Double.parseDouble(costumer_conv_lat);
        costumer_receiver_longitude = Double.parseDouble(costumer_conv_long);

        distanceText = (TextView)findViewById(R.id.distance_viewer);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        polylines = new ArrayList<>();

        final LatLng sydney = new LatLng(receive_latitude, receive_longitude);

        customer = new LatLng(costumer_receiver_latitude,costumer_receiver_longitude);

        //DRAW LINES DIRECTION BETWEEN SHOP AND CUSTOMER
        StartRouting(customer,sydney);

        mMap.addMarker(new MarkerOptions().position(customer).title("YOU"));

        //Get distance between two points

        Location loc1 = new Location("");
        loc1.setLatitude(sydney.latitude);
        loc1.setLatitude(sydney.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(customer.latitude);
        loc2.setLatitude(customer.longitude);

        distance = loc1.distanceTo(loc2);

        distanceText.setText(convert_shop_label +" Found : "+ String.valueOf(distance));




        /*Target mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Marker driver_marker = mMap.addMarker(new MarkerOptions()
                        .position(sydney)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title(convert_shop_label)
                        .snippet("Here is our address")
                );
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.d("picasso", "onBitmapFailed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.get()
                .load(convert_logo)
                .resize(250,250)
                .centerCrop()
                .transform(new BubbleTransformation(20))
                .into(mTarget);

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(receive_latitude, receive_longitude);*/
        mMap.addMarker(new MarkerOptions().position(sydney).title(convert_shop_label));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
    }

    @Override
    public void onLocationChanged(Location location) {
        distanceText.setText(convert_shop_label +" Found : "+ String.valueOf(distance));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(customer));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortest_way) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    public void StartRouting(LatLng custSource, LatLng shopDestination){
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(custSource, shopDestination)
                .build();
        routing.execute();
    }

    public void erasePolyLines(){
        for (Polyline line : polylines){
            line.remove();
        }

        polylines.clear();
    }
}
