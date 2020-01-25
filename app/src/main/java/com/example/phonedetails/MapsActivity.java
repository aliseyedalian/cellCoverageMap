package com.example.phonedetails;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
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

    private static final String TAG = "MyMapsActivity";
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private TelephonyManager telephonyManager;
    private Location lastLocation;
    TextView txt;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        txt = findViewById(R.id.txt);

    }

    @Override
    protected void onResume() {
        super.onResume();
        statusCheck();
    }

    public void statusCheck() {
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
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



    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                ==PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onMapReady: mMap.setMyLocationEnabled(true);");
            mMap.setMyLocationEnabled(true);
        }
        if(lastLocation!=null){
            Log.d(TAG, "onMapReady: go to location");
            LatLng latLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomBy(18));
        }
//        drawPreviousSignalStrengthCirclesFromDB();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                ==PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,
                    this);
        }
    }


    @Override
    public void onLocationChanged(Location newLocation) {
        Log.d(TAG, "onLocationChanged: ");
        lastLocation = newLocation;
        txt.append(newLocation.getLatitude()+":"+newLocation.getLongitude()+"\n");
        // Showing the new location in Google Map
        LatLng newLatLng = new LatLng(newLocation.getLatitude(),newLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(18));

        if(googleApiClient != null){
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
                int cellid = ((CellInfoGsm)cellInfo).getCellIdentity().getCid();
                int rssi = ((CellInfoGsm)cellInfo).getCellSignalStrength().getDbm();
                if(rssi > maxSS2G) maxSS2G = rssi;
                Log.d(TAG, "startStopFunction: 2G-cellid="+cellid+"/cell_ss="+rssi);
            }
            else if(cellInfo instanceof CellInfoLte){  //if LTE connection(4G)
                int cellid = ((CellInfoLte)cellInfo).getCellIdentity().getCi();
                int rssi = ((CellInfoLte)cellInfo).getCellSignalStrength().getDbm();
                if(rssi > maxSS4G) maxSS4G = rssi;
                Log.d(TAG, "startStopFunction: 4G-cellid="+cellid+"/cell_ss="+rssi);
            }
            else if (cellInfo instanceof CellInfoWcdma){  //if wcdma connection(3G)
                int cellid = ((CellInfoWcdma)cellInfo).getCellIdentity().getCid();
                int rssi = ((CellInfoWcdma)cellInfo).getCellSignalStrength().getDbm();
                if(rssi > maxSS3G) maxSS3G = rssi;
                Log.d(TAG, "startStopFunction: 3G-cellid="+cellid+"/cell_ss="+rssi);
            }
        }
        Log.d(TAG, "startStopFunction: maxSS26="+maxSS2G);
        //registerToDb(latLng.latitude,latLng.longitude,maxSS2G);
        Log.d(TAG, "startStopFunction: maxSS36="+maxSS3G);
        //registerToDb(latLng.latitude,latLng.longitude,maxSS3G);
        /* now only draw new signal coverage for 4G  **/
        Log.d(TAG, "startStopFunction: maxSS46="+maxSS4G);
        int level4G = dBmToLevel(maxSS4G);
        registerToDb(newLatLng.latitude,newLatLng.longitude,level4G);
        drawSignalCoverageCircle(level4G,newLatLng);
        //addNewRecordToDB(lng,lat,signalLevel,avgSs,opr,gen);
    }

    private void registerToDb(double latitude, double longitude, int level) {
        Log.d(TAG, "registerToDb: ");
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

    private void drawSignalCoverageCircle(int level, LatLng latLng) {
        Log.d(TAG, "drawSignalCoverageCircle: (level:"+level+","+latLng+")");
        int radius=5,alpha=120,red=255,green=255,blue=255;
        switch (level){
            case -1:
                return;
            case 0:
                red = 255;
                green = 0;
                blue = 0;
                break;
            case 1:
                red = 204;
                green = 51;
                blue = 0;
                break;
            case 2:
                red = 153;
                green = 102;
                blue = 0;
                break;
            case 3:
                red = 102;
                green = 153;
                blue = 0;
                break;
            case 4:
                red = 51;
                green = 204;
                blue = 0;
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

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(18));
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
