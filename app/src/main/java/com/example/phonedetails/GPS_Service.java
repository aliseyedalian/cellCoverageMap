package com.example.phonedetails;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

public class GPS_Service extends Service {
    private LocationListener locationListener;
    private LocationManager locationManager;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        Log.d("gps", "onCreate");
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("gps", "onLocationChanged: "+location.getLongitude()+":"+location.getLatitude());
                Intent i = new Intent("location_update");
                i.putExtra("longitude",location.getLongitude());
                i.putExtra("latitude",location.getLatitude());
                sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d("gps", "onStatusChanged: "+s+":"+i+":"+bundle);
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d("gps", "onProviderEnabled: "+s);
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("gps", "onProviderDisabled: "+s);
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000,0,locationListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("gps", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("gps", "onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("gps", "onDestroy");
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }
}
