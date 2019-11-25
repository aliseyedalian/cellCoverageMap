package com.example.phonedetails;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener {

    private static final String TAG = "mapp";
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    Button btnStartStop;
    String startStopStr = "start";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(googleApiClient == null){
            buildGoogleApiClient();
        }
        mMap.setMyLocationEnabled(true);
        btnStartStop = findViewById(R.id.btnStartStop);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStop();
            }
        });
//        drawPreviousSignalStrengthCirclesFromDB();
    }

    @SuppressLint("SetTextI18n")
    private void startStop() {
        if (startStopStr.equals("start")) {
            startStopStr = "stop";
            btnStartStop.setText("Stop");
            btnStartStop.setBackgroundColor(Color.parseColor("#D82B1B"));
//            addSignalStrengthCircle(5,
//                    new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()));
        }else if(startStopStr.equals("stop")){
            startStopStr = "start";
            btnStartStop.setText("Start");
            btnStartStop.setBackgroundColor(Color.parseColor("#359c5e"));
        }else{
            Log.e(TAG, "startStop: invalid startStopStr");
        }
        Log.d(TAG, "startStop: startStopStr="+startStopStr);
    }
    private void addSignalStrengthCircle(int signalLevel,LatLng latLng) {
        int radius=30,alpha=120,red=255,green=255,blue=255;
        switch (signalLevel){
            case 0:
                red = 255;
                green = 0;
                blue = 0;
                break;
            case 1:
                red = 255;
                green = 62;
                blue = 0;
                break;
            case 2:
                red = 255;
                green = 152;
                blue = 0;
                break;
            case 3:
                red = 255;
                green = 229;
                blue = 0;
                break;
            case 4:
                red = 177;
                green = 255;
                blue = 88;
                break;
            case 5:
                red = 0;
                green = 255;
                blue = 0;
                break;
        }
        CircleOptions circleOptions = new CircleOptions().
                center(latLng).
                radius(radius).
                fillColor(Color.argb(alpha,red,green,blue)).strokeWidth(0);
        mMap.addCircle(circleOptions);
    }
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        if(lastLocation!=null)
        Log.d(TAG, " --> lastLocation: "+lastLocation.getLatitude()+"/"+lastLocation.getLongitude());
        Log.d(TAG, " --> location: "+location.getLatitude()+"/"+location.getLongitude());
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(15));
        if(googleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }
        if(startStopStr.equals("stop")){
            double lng = location.getLongitude();
            double lat = location.getLatitude();
            int AvgSS;
            String Opr;
            String Gen;
            //save to db...
            //draw ss circle
            //obtain signal level from AvgSS
            int signalLevel=5;
            addSignalStrengthCircle(signalLevel,latLng);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        LocationRequest locationRequest = new LocationRequest().setInterval(700).setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,this);
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult){
        Log.d(TAG, "onConnectionFailed");
    }
    @Override
    public void onMapLongClick(LatLng point) {
        Log.d(TAG, "onMapLongClick");
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick");
        return true;
    }
    @Override
    public void onMapClick(LatLng point) {
        Log.d(TAG, "onMapClick");
    }
    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
}
