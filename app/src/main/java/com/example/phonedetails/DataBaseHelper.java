package com.example.phonedetails;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.support.constraint.Constraints.TAG;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BLE_Scenarios.db";
    private SQLiteDatabase myDb = this.getWritableDatabase();

    DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTables(sqLiteDatabase);
    }
    private void createTables(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("create table posLev(" +
                "lat REAL," +
                "lng REAL,"+
                "lev INTEGER," +
                "constraint pos primary key (lat,lng)" +
                ");"
        );
    }

    private boolean isExistPos(double lat,double lng) {
        String query = "select * from posLev where " +
                "lat ="+lat+" and lng="+lng+";";
        @SuppressLint("Recycle") Cursor resultCursor = myDb.rawQuery(query,null);
        return resultCursor.getCount()!=0;
    }

    boolean insertPosLev(double lat,double lng,int lev){
        if(isExistPos(lat,lng)){
            Log.d(TAG,"insertPosLev : Existed position");
            return false;
        }
        ContentValues contentValuesPhone = new ContentValues();
        contentValuesPhone.put("lat",lat);
        contentValuesPhone.put("lng",lng);
        contentValuesPhone.put("lev",lev);
        long resultPhone = myDb.insert("posLev",null,contentValuesPhone);
        return resultPhone!=-1;
    }



    Cursor getPosLevTable(){
        return myDb.rawQuery("select * from posLev",null);
    }

    private void rebuild() {
        myDb.execSQL("drop table if exists poslev");
        onCreate(myDb);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("drop table if exists poslev");
        onCreate(sqLiteDatabase);
    }
}

