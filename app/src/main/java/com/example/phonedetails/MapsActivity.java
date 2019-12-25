package com.example.phonedetails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
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
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private Button btnStartStop;
    private boolean isStart = false;
    private TelephonyManager manager;


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
        if (googleApiClient == null) {
            buildGoogleApiClient();
        }
        mMap.setMyLocationEnabled(true);
        btnStartStop = findViewById(R.id.btnStartStop);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopFunction();
            }
        });
        manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//        drawPreviousSignalStrengthCirclesFromDB();
    }

    @SuppressLint("SetTextI18n")
    private void startStopFunction() {
        if (isStart) {
            isStart = false;
            btnStartStop.setText("Stop");
            btnStartStop.setTextColor(Color.parseColor("#D82B1B"));
            @SuppressLint("MissingPermission") List<CellInfo> allCellInfo = manager.getAllCellInfo();
            LatLng latLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
            int maxSS2G=-200,maxSS3G=-200,maxSS4G=-200;
            for (int i = 0; i < allCellInfo.size(); ++i) {
                CellInfo cellInfo = allCellInfo.get(i);
                if (cellInfo instanceof CellInfoGsm){ //if GSM connection(2G)
                    int cellid = ((CellInfoGsm)cellInfo).getCellIdentity().getCid();
                    CellSignalStrengthGsm cellss = ((CellInfoGsm)cellInfo).getCellSignalStrength();
                    int SS2GdBm = cellss.getDbm();
                    if(SS2GdBm > maxSS2G) maxSS2G = SS2GdBm;
                }
                else if(cellInfo instanceof CellInfoLte){  //if LTE connection(4G)
                    int cellid = ((CellInfoLte)cellInfo).getCellIdentity().getCi();
                    CellSignalStrengthLte cellss = ((CellInfoLte)cellInfo).getCellSignalStrength();
                    int SS4GdBm = cellss.getDbm();
                    if(SS4GdBm > maxSS4G) maxSS4G = SS4GdBm;
                }
                else if (cellInfo instanceof CellInfoWcdma){  //if wcdma connection(3G)
                    int cellid = ((CellInfoWcdma)cellInfo).getCellIdentity().getCid();
                    CellSignalStrengthWcdma cellss = ((CellInfoWcdma)cellInfo).getCellSignalStrength();
                    int SS3GdBm = cellss.getDbm();
                    if(SS3GdBm > maxSS3G) maxSS3G = SS3GdBm;
                }
            }
            Log.d(TAG, "startStopFunction: maxSS26="+maxSS2G);
            Log.d(TAG, "startStopFunction: maxSS36="+maxSS3G);
            Log.d(TAG, "startStopFunction: maxSS46="+maxSS4G);
        }else{
            isStart = true;
            btnStartStop.setText("Start");
            btnStartStop.setTextColor(Color.parseColor("#359c5e"));

        }
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
        if(lastLocation!=null){
            Log.d(TAG, " --> lastLocation: "+lastLocation.getLatitude()+"/"+lastLocation.getLongitude());
        }
        Log.d(TAG, " --> location: "+location.getLatitude()+"/"+location.getLongitude());
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(15));
        if(googleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }
        if(!isStart){
            @SuppressLint("MissingPermission") List<CellInfo> allCellInfo = manager.getAllCellInfo();
            int maxSS2G=-200,maxSS3G=-200,maxSS4G=-200;
            for (int i = 0; i < allCellInfo.size(); ++i) {
                CellInfo cellInfo = allCellInfo.get(i);
                if (cellInfo instanceof CellInfoGsm){ //if GSM connection(2G)
                    int cellid = ((CellInfoGsm)cellInfo).getCellIdentity().getCid();
                    CellSignalStrengthGsm cellss = ((CellInfoGsm)cellInfo).getCellSignalStrength();
                    int SS2GdBm = cellss.getDbm();
                    if(SS2GdBm > maxSS2G) maxSS2G = SS2GdBm;
                }
                else if(cellInfo instanceof CellInfoLte){  //if LTE connection(4G)
                    int cellid = ((CellInfoLte)cellInfo).getCellIdentity().getCi();
                    CellSignalStrengthLte cellss = ((CellInfoLte)cellInfo).getCellSignalStrength();
                    int SS4GdBm = cellss.getDbm();
                    if(SS4GdBm > maxSS4G) maxSS4G = SS4GdBm;
                }
                else if (cellInfo instanceof CellInfoWcdma){  //if wcdma connection(3G)
                    int cellid = ((CellInfoWcdma)cellInfo).getCellIdentity().getCid();
                    CellSignalStrengthWcdma cellss = ((CellInfoWcdma)cellInfo).getCellSignalStrength();
                    int SS3GdBm = cellss.getDbm();
                    if(SS3GdBm > maxSS3G) maxSS3G = SS3GdBm;
                }
            }
            Log.d(TAG, "startStopFunction: maxSS26="+maxSS2G);
            Log.d(TAG, "startStopFunction: maxSS36="+maxSS3G);
            Log.d(TAG, "startStopFunction: maxSS46="+maxSS4G);

            int signalLevel2G = dBmToSignalLevel(maxSS4G);
            //addNewRecordToDB(lng,lat,signalLevel,avgSs,opr,gen);
            addSignalStrengthCircle(signalLevel2G,latLng);
//            int signalLevel3G = dBmToSignalLevel(maxSS3G);
//            //addNewRecordToDB(lng,lat,signalLevel,avgSs,opr,gen);
//            addSignalStrengthCircle(signalLevel3G,latLng);
//            int signalLevel4G = dBmToSignalLevel(maxSS4G);
//            //addNewRecordToDB(lng,lat,signalLevel,avgSs,opr,gen);
//            addSignalStrengthCircle(signalLevel4G,latLng);
        }
    }

    private int dBmToSignalLevel(int dBm) {
        if(dBm < 0 && dBm >= -55) return 5;
        if(dBm < -55 && dBm >= -65) return 4;
        if(dBm < -65 && dBm >= -75) return 3;
        if(dBm < -75 && dBm >= -85) return 2;
        if(dBm < -85 && dBm >= -120) return 1;
        else return 0;
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
