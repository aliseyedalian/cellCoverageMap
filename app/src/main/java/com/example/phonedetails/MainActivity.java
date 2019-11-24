package com.example.phonedetails;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "main";
    Button btnStart;
    Button btnClean;
    Button btnLocStart;
    Button btnLocStop;
    Button btnMap;
    TextView tvInfo;
    TextView tvLocation;
    TextView tvLongitude;
    TextView tvLatitude;
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
        tvLocation = findViewById(R.id.tv_location);
        tvLatitude=findViewById(R.id.tv_latitude);
        tvLongitude=findViewById(R.id.tv_longitude);
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
        btnLocStart = findViewById(R.id.btnLocStart);
        btnLocStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvLocation.setTextColor(Color.parseColor("#00FF00"));
                GPS_start();
            }
        });
        btnLocStop = findViewById(R.id.btnLocStop);
        btnLocStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvLocation.setTextColor(Color.parseColor("#FF0000"));
                GPS_stop();
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
        Log.d(TAG, "MyTelephonyManager");
        int phoneType = manager.getPhoneType();
        switch (phoneType) { //phoneType indicates the type of radio used to transmit voice calls.
            case (TelephonyManager.PHONE_TYPE_CDMA):
                strPhoneType = "CDMA";
                break;
            case (TelephonyManager.PHONE_TYPE_GSM):
                strPhoneType = "GSM";
                break;
            case (TelephonyManager.PHONE_TYPE_SIP):
                strPhoneType = "SIP";
                break;
            case (TelephonyManager.PHONE_TYPE_NONE):
                strPhoneType = "NONE";
                break;
        }

        boolean isRoaming = manager.isNetworkRoaming();
        String PhoneType = strPhoneType;
        @SuppressLint("MissingPermission") String imei = manager.getImei();//International Mobile Equipment Identity (IMEI)
        @SuppressLint({"HardwareIds", "MissingPermission"}) String subscriberId = manager.getSubscriberId(); //IMSI
        @SuppressLint({"HardwareIds", "MissingPermission"}) String simSerialNumber = manager.getSimSerialNumber();
        String networkCountryIso = manager.getNetworkCountryIso();
        String simCountryIso = manager.getSimCountryIso();
        String networkOperatorName = manager.getNetworkOperatorName();
        @SuppressLint("MissingPermission") CellLocation cellLocation = manager.getCellLocation();

        info += "\nnetwork Operator Name: " + networkOperatorName;
        info += "\nPhone Network Type: " + PhoneType;
        info += "\nIMEI: " + imei;
        info += "\nIMSI: " + subscriberId;
        info += "\nSIM Serial Number: " + simSerialNumber;
        info += "\nNetwork Country ISO: " + networkCountryIso;
        info += "\nSIM Country ISO: " + simCountryIso;
        info += "\nCell Location: " + cellLocation;
        info += "\nRoaming: " + isRoaming;


        @SuppressLint("MissingPermission") List<CellInfo> allCellInfo = manager.getAllCellInfo();
        @SuppressLint("MissingPermission") List<NeighboringCellInfo> neighboringCellInfo = manager.getNeighboringCellInfo();
        info += "\n\nAllCellInfo:\n\n";
        for (int i = 0; i < allCellInfo.size(); ++i) {
            try {
                CellInfo cellInfo = allCellInfo.get(i);
                Log.d(TAG, "MyTelephonyManager: cellInfo_"+i+"="+ cellInfo);
                if (cellInfo instanceof CellInfoGsm){ //if GSM connection
                    CellSignalStrengthGsm signalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength();
                    CellIdentityGsm identityGsm = ((CellInfoGsm) cellInfo).getCellIdentity();
                    info += "Cell_"+i + "{\r\n";
                    info += "   GSM\r\n";
                    info += "   CellID: "+ identityGsm.getCid() + "\r\n";
                    info += "   Registered: " + cellInfo.isRegistered() + "\r\n";
                    info += "   SS: " + signalStrength.getDbm() + "dBm}\r\n\r\n";
                    //call whatever you want from gsm / identitydGsm
                }
                else if(cellInfo instanceof CellInfoLte){  //if LTE connection
                    CellSignalStrengthLte signalStrength = ((CellInfoLte) cellInfo).getCellSignalStrength();
                    CellIdentityLte identityLte = ((CellInfoLte) cellInfo).getCellIdentity();
                    info += "Cell_"+i + "{\r\n";
                    info += "   LTE\r\n";
                    info += "   CellID: "+ identityLte.getCi() + "\r\n";
                    info += "   Registered: " + cellInfo.isRegistered() + "\r\n";
                    info += "   SS: " + signalStrength.getDbm() + "dBm}\r\n\r\n";
                    //call whatever you want from lte / identityLte
                }
                else if (cellInfo instanceof CellInfoWcdma){  //if wcdma connection
                    CellSignalStrengthWcdma signalStrength= ((CellInfoWcdma) cellInfo).getCellSignalStrength();
                    CellIdentityWcdma identityWcdma = ((CellInfoWcdma)cellInfo).getCellIdentity();
                    info += "Cell_"+i + "{\r\n";
                    info += "   WCDMA\r\n";
                    info += "   CellID: "+ identityWcdma.getCid() + "\r\n";
                    info += "   Registered: " + cellInfo.isRegistered() + "\r\n";
                    info += "   SS: " + signalStrength.getDbm() + "dBm}\r\n\r\n";
                    //call whatever you want from wcdmaS / wcdmaid
                }
//                else if (cellInfo instanceof CellInfoCdma){  //if cdma connection
//                    CellSignalStrengthCdma signalStrength= ((CellInfoCdma) cellInfo).getCellSignalStrength();
//                    CellIdentityCdma identityCdma = ((CellInfoCdma)cellInfo).getCellIdentity();
//                    info += "Cell_"+i + "{\r\n";
//                    info += "   CDMA\r\n";
//                    info += "   CellID: "+ identityCdma + "\r\n";
//                    info += "   Registered: " + cellInfo.isRegistered() + "\r\n";
//                    info += "   SS: " + signalStrength.getDbm() + "dBm}\r\n\r\n";
//                    //call whatever you want from cdmaS / cdmaid
//                }
            }catch (Exception ex){
                Log.d("neighboring error: ",ex.getMessage());
            }
        }
        tvInfo.setText(info);
    }

    public void clean() {
        info = "";
        tvInfo.setText("");
    }

    @SuppressLint("MissingPermission")
    public void GPS_start(){
        Log.d("gps", "GPS_start");
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: "+location.getLongitude()+":"+location.getLatitude());
                tvLatitude.setText((int) location.getLatitude());
                tvLongitude.setText((int) location.getLongitude());
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
            }
        };
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,0,locationListener);
    }
    public void GPS_stop(){
        Log.d("gps", "GPS_stop");
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }
}

////permission check
//    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            ActivityCompat.requestPermissions((Activity)this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
//
//            String list = "";  //I'm just adding everything to a string to display, but you can do whatever
//
//            //get cell info
//            TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//            List<CellInfo> infos = tel.getAllCellInfo();
//        for (int i = 0; i<infos.size(); ++i)
//        {
//        try {
//        CellInfo info = infos.get(i);
//        if (info instanceof CellInfoGsm) //if GSM connection
//        {
//        list += "Site_"+i + "\r\n";
//        list += "Registered: " + info.isRegistered() + "\r\n";
//        CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
//        CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
//        list += "cellID: "+ identityGsm.getCid() + "\r\n";
//        list += "dBm: " + gsm.getDbm() + "\r\n\r\n";
//        //call whatever you want from gsm / identitydGsm
//        }
//        else if (info instanceof CellInfoLte)  //if LTE connection
//        {
//        list += "Site_"+i + "\r\n";
//        list += "Registered: " + info.isRegistered() + "\r\n";
//        CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
//        CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
//        //call whatever you want from lte / identityLte
//        }
//        else if (info instanceof CellInfoWcdma)  //if wcdma connection
//        {
//        CellSignalStrengthWcdma wcdmaS = ((CellInfoWcdma) info).getCellSignalStrength();
//        CellIdentityWcdma wcdmaid = ((CellInfoWcdma)info).getCellIdentity();
//        list += "Site_"+i + "\r\n";
//        list += "Registered: " + info.isRegistered() + "\r\n";
//        //call whatever you want from wcdmaS / wcdmaid
//
//        }
//
//        } catch (Exception ex) {
//        Log.i("neighboring error 2: " ,ex.getMessage());
//        }
//        }
//        Log.i("Info display", list);  //display everything.