package com.example.phonedetails;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
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

@SuppressWarnings("ALL")
public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener {

    private static final String TAG = "logD";
    private static final int DEFAULT_ZOOM = 5;
    private static LocationRequest locationRequest;
    private final LatLng mDefaultLocation = new LatLng(32.6027, 55.0197);
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private TelephonyManager telephonyManager;
    private Location mLastKnownLocation;
    DataBaseHelper databaseHelper;
    TextView txt;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private com.google.android.gms.location.LocationListener locationListener;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MAP onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (fragment != null) {
            Log.d(TAG, "MAP Map Async");
            fragment.getMapAsync(this);
        }
        // telephonyManager used for get cells information
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        txt = findViewById(R.id.txt);
        //databaseHelper use for handle database functions
        databaseHelper = new DataBaseHelper(this);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude=location.getLatitude();
                double longitude=location.getLongitude();
                txt.append("New Latitude: "+latitude + "New Longitude: "+longitude);
            }
        };

    }


//


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "MAP onMapReady");
        mMap = googleMap;
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            Log.d(TAG, "MAP onMapReady: enable my location button");
        } catch (SecurityException e)  {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            Log.d(TAG,"MAP onMapReady: can not enable my location button,"+ e.getMessage());
        }
        drawCoverageCirclesFromDB();
    }



    private void drawCoverageCirclesFromDB() {
        Log.d(TAG, "MAP drawCoverageCirclesFromDB: ");
        Cursor resultCursor = databaseHelper.getPosLevTable();
        if(resultCursor.getCount()==0){
            Log.d(TAG, "MAP drawCoverageCirclesFromDB: empty result, return");
            return;
        }
        while (resultCursor.moveToNext()){
            double lat = resultCursor.getDouble(0) ;
            double lng = resultCursor.getDouble(1);
            int lev = resultCursor.getInt(2);
            LatLng latLng = new LatLng(lat,lng);
            drawCoverageCircle(lev,latLng);
            Log.d(TAG, "MAP draw db Circles: lev="+lev+",latLng="+latLng);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "MAP onConnected: ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        final PendingResult locationUpdatesResult = LocationServices.FusedLocationApi
                .requestLocationUpdates(googleApiClient, locationRequest, this);
        PendingResult.StatusListener statusListener = new PendingResult.StatusListener() {
            @Override
            public void onComplete(Status status) {
                Log.d(TAG, "onComplete: status="+status);
            }
        };
        locationUpdatesResult.addStatusListener(statusListener);
        new Thread(new Runnable() {
            public void run(){
                locationUpdatesResult.await();
            }
        }).start();
    }


    @Override
    public void onLocationChanged(Location newLocation) {
        Log.d(TAG, "onLocationChanged: ");
        mLastKnownLocation = newLocation;
        txt.append(newLocation.getLatitude()+":"+newLocation.getLongitude()+"\n");
        // Showing the new location in Google Map
        LatLng newLatLng = new LatLng(newLocation.getLatitude(),newLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(18));

        if(googleApiClient != null){
            //noinspection deprecation
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onLocationChanged: return; Permission missed!");
            return;
        }
        List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
        int maxSS2G=-200,maxSS3G=-200,maxSS4G=-200;
        for (int i = 0; i < allCellInfo.size(); i++) {
            CellInfo cellInfo = allCellInfo.get(i);
            //Log.d(TAG, "startStopFunction: cellInfo "+i+"{"+cellInfo+"}");
            if (cellInfo instanceof CellInfoGsm){ //if GSM connection(2G)
                int cell_id = ((CellInfoGsm)cellInfo).getCellIdentity().getCid();
                int rssi = ((CellInfoGsm)cellInfo).getCellSignalStrength().getDbm();
                if(rssi > maxSS2G) maxSS2G = rssi;
                Log.d(TAG, "startStopFunction: 2G-cell_id="+cell_id+"/cell_ss="+rssi);
            }
            else if(cellInfo instanceof CellInfoLte){  //if LTE connection(4G)
                int cell_id = ((CellInfoLte)cellInfo).getCellIdentity().getCi();
                int rssi = ((CellInfoLte)cellInfo).getCellSignalStrength().getDbm();
                if(rssi > maxSS4G) maxSS4G = rssi;
                Log.d(TAG, "startStopFunction: 4G-cell_id="+cell_id+"/cell_ss="+rssi);
            }
            else if (cellInfo instanceof CellInfoWcdma){  //if wcdma connection(3G)
                int cell_id = ((CellInfoWcdma)cellInfo).getCellIdentity().getCid();
                int rssi = ((CellInfoWcdma)cellInfo).getCellSignalStrength().getDbm();
                if(rssi > maxSS3G) maxSS3G = rssi;
                Log.d(TAG, "startStopFunction: 3G-cell_id="+cell_id+"/cell_ss="+rssi);
            }
        }
        Log.d(TAG, "startStopFunction: maxSS26="+maxSS2G);
        //saveToDb(latLng.latitude,latLng.longitude,maxSS2G);
        Log.d(TAG, "startStopFunction: maxSS36="+maxSS3G);
        //saveToDb(latLng.latitude,latLng.longitude,maxSS3G);
        /* now only draw new signal coverage for 4G  **/
        Log.d(TAG, "startStopFunction: maxSS46="+maxSS4G);
        int level4G = dBmToLevel(maxSS4G);
        saveToDb(newLatLng.latitude,newLatLng.longitude,level4G);
        drawCoverageCircle(level4G,newLatLng);
        //addNewRecordToDB(lng,lat,signalLevel,avgSs,opr,gen);
    }

    private void saveToDb(double latitude, double longitude, int level) {
        Log.d(TAG, "saveToDb: ");
        if(!databaseHelper.insertPosLev(latitude,longitude,level)){
            Log.d(TAG, "MAP saveToDb: lat="+latitude+",lng="+longitude+",lev="+level);
        }
    }

    private int dBmToLevel(int dBm) { //5,4,3,2,1,0
        Log.d(TAG, "dBmToLevel: ");
        if(dBm < 0 && dBm >= -60) return 5;
        else if(dBm < -60 && dBm >= -75) return 4;
        else if(dBm < -75 && dBm >= -95) return 3;
        else if(dBm < -95 && dBm >= -110) return 2;
        else if(dBm < -110 && dBm >= -120) return 1;
        else if(dBm < -120) return 0;
        else return -1;
    }

    private void drawCoverageCircle(int level, LatLng latLng) {
        Log.d(TAG, "MAP drawCoverageCircle: (level:"+level+","+latLng+")");
        int radius=10,alpha=120,red=255,green=255,blue=255;
        switch (level){
            case -1:
                return;
            case 0:
                red = 100;
                green = 0;
                blue = 0;
                alpha=120;
                break;
            case 1:
                red = 120;
                green = 0;
                blue = 0;
                alpha=100;
                break;
            case 2:
                red = 20;
                green = 100;
                blue = 0;
                alpha=50;
                break;
            case 3:
                red = 0;
                green = 150;
                blue = 0;
                alpha=70;
                break;
            case 4:
                red = 0;
                green = 200;
                blue = 0;
                alpha=100;
                break;
            case 5:
                red = 0;
                green = 255;
                blue = 0;
                alpha=140;
                break;
        }
        CircleOptions circleOptions = new CircleOptions().
                center(latLng).
                radius(radius).
                fillColor(Color.argb(alpha,red,green,blue)).strokeWidth(0);
        mMap.addCircle(circleOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(18));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gpsCheck();
    }

    public void gpsCheck() {
        Log.d(TAG, "MAP gpsCheck");
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled, Do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }









    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult){
        Log.d(TAG, "onConnectionFailed: ");
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
    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop: ");
        googleApiClient.disconnect();
    }
}
