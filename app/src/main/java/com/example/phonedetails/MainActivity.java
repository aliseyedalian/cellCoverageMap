package com.example.phonedetails;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "main";
    Button btnStart;
    Button btnClean;
    Button btnMap;
    TextView tvInfo;
    String info = "";
    String strPhoneType = "";
    TelephonyManager manager;
    LocationListener locationListener;
    LocationManager locationManager;



    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvInfo = findViewById(R.id.tv_info);
        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                clean();
                MyTelephonyManager();
            }
        });
        btnClean = findViewById(R.id.btnClean);
        btnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clean();
            }
        });
        btnMap = findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,MapsActivity.class));
            }
        });
        manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        check_READ_PHONE_STATE_permission();
    }

    private void check_ACCESS_LOCATION_permissions() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "permissions: Location permission requested.");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},200);
        }
    }

    private void check_READ_PHONE_STATE_permission() {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "permissions: READ_PHONE_STATE permission requested.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},100);
        }
    }


    @SuppressLint("ShowToast")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case (100): //Read Phone State
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "onRequestPermissionsResult: READ_PHONE_STATE permission granted.");
                    check_ACCESS_LOCATION_permissions();
                }else {
                    Log.d(TAG, "onRequestPermissionsResult: READ_PHONE_STATE permission denied.finish.");
                    finish();
                }
                break;
            case (200)://Access Location
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "onRequestPermissionsResult: Access Location permissions granted.");
                }else {
                    Log.d(TAG, "onRequestPermissionsResult: Access Location permissions denied.finish.");
                    finish();
                }
                break;

        }
    }



    @TargetApi(Build.VERSION_CODES.P)
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void MyTelephonyManager() {
//        Log.d(TAG, "MyTelephonyManager");
//        int phoneType = manager.getPhoneType();
//        switch (phoneType) { //phoneType indicates the type of radio used to transmit voice calls.
//            case (TelephonyManager.PHONE_TYPE_CDMA):
//                strPhoneType = "CDMA";
//                break;
//            case (TelephonyManager.PHONE_TYPE_GSM):
//                strPhoneType = "GSM";
//                break;
//            case (TelephonyManager.PHONE_TYPE_SIP):
//                strPhoneType = "SIP";
//                break;
//            case (TelephonyManager.PHONE_TYPE_NONE):
//                strPhoneType = "NONE";
//                break;
//        }

//        boolean isRoaming = manager.isNetworkRoaming();
//        String PhoneType = strPhoneType;
//        @SuppressLint("MissingPermission") String imei = manager.getImei();//International Mobile Equipment Identity (IMEI)
//        @SuppressLint({"HardwareIds", "MissingPermission"}) String subscriberId = manager.getSubscriberId(); //IMSI
//        @SuppressLint({"HardwareIds", "MissingPermission"}) String simSerialNumber = manager.getSimSerialNumber();
//        String networkCountryIso = manager.getNetworkCountryIso();
//        String simCountryIso = manager.getSimCountryIso();
        String networkOperatorName = manager.getNetworkOperatorName();
        String networkOperator = manager.getNetworkOperator();
        info += "network Operator : " + networkOperator;
        info += "\nnetwork Operator Name: " + networkOperatorName;
//        info += "\nPhone Network Type: " + PhoneType;
//        info += "\nIMEI: " + imei;
//        info += "\nIMSI: " + subscriberId;
//        info += "\nSIM Serial Number: " + simSerialNumber;
//        info += "\nNetwork Country ISO: " + networkCountryIso;
//        info += "\nSIM Country ISO: " + simCountryIso;
//        info += "\nRoaming: " + isRoaming;

        @SuppressLint("MissingPermission") List<CellInfo> allCellInfo = manager.getAllCellInfo();
        info += "\n\nAllCellInfo:\n\n";
        int count2G = 0;
        int count3G = 0;
        int count4G = 0;
        int sumSS2G = 0;
        int sumSS3G = 0;
        int sumSS4G = 0;
        int avgSS2G;
        int avgSS3G;
        int avgSS4G;

        for (int i = 0; i < allCellInfo.size(); ++i) {
            CellInfo cellInfo = allCellInfo.get(i);
            if (cellInfo instanceof CellInfoGsm){ //if GSM connection(2G)
                count2G++;
                int SS2GdBm = ((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm();
                sumSS2G += SS2GdBm;
            }
            else if(cellInfo instanceof CellInfoLte){  //if LTE connection(4G)
                count4G++;
                int SS4GdBm = ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();
                sumSS4G += SS4GdBm;
            }
            else if (cellInfo instanceof CellInfoWcdma){  //if wcdma connection(3G)
                count3G++;
                int SS3GdBm = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm();
                sumSS4G += SS3GdBm;
            }
        }
        if(count2G != 0){
            avgSS2G = sumSS2G / count2G;
            info += "\navgSS2G = " + avgSS2G;
        }
        if(count3G != 0){
            avgSS3G = sumSS3G / count3G;
            info += "\navgSS3G = " + avgSS3G;
        }
        if(count4G != 0){
            avgSS4G = sumSS4G / count4G;
            info += "\navgSS4G = " + avgSS4G;
        }

        tvInfo.setText(info);
    }

    public void clean() {
        info = "";
        tvInfo.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }
}
