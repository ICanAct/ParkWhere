package com.example.parkwhere;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;


public class DBController extends SQLiteOpenHelper {
    private Context context;
    private ArrayList<CarPark> nearbyCarParks = new ArrayList<>();
    private String path = "carpark_info.xls";;
    protected SQLiteDatabase mDefaultWritableDatabase = this.getWritableDatabase();
    protected DBController(Context applicationcontext){
        super(applicationcontext, "CarparkDB.db", null, 1);
        // DATABASE is being created.
        context = applicationcontext;


    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query_carpark, query_avail;
        query_carpark = "CREATE TABLE IF NOT EXISTS carparks (car_park_no VARCHAR PRIMARY KEY, address VARCHAR, Latitude REAL, Longitude REAL, " +
                "car_park_type VARCHAR, type_of_parking_system VARCHAR, short_term_parking VARCHAR, free_parking VARCHAR, night_parking VARCHAR, car_park_decks REAL, gantry_height REAL, car_park_basement VARCHAR)";
        query_avail = "CREATE TABLE IF NOT EXISTS availability (car_park_no VARCHAR, total_lots INTEGER, lot_type VARCHAR, lots_available INTEGER, PRIMARY KEY(car_park_no, lot_type))";
        db.execSQL(query_carpark);
        db.execSQL(query_avail);
       // readXLS(db,path, "carparks");


    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query;
        query = "DROP TABLE IF EXISTS carparks"; // UPDATE THE QUERY STATEMENT
        db.execSQL(query);
        db.execSQL("DROP TABLE IF EXISTS availability");

        onCreate(db);

    }

    public ArrayList<CarPark> getCarparks(LatLng latLng){ // RETURNS THE ARRAY OF CARPARKS NEARBY THE LOCATION
        double user_lat = latLng.latitude;        // FOR the query part please change accordingly
        double user_lng = latLng.longitude;       // to the radius calculated.
        float[] results = new float[1];
        String selectQuery = "SELECT latitude, longitude FROM carparks";
        //SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = mDefaultWritableDatabase.rawQuery(selectQuery, null);
        while(cursor.moveToNext()){
            int index = cursor.getColumnIndexOrThrow("Latitude");
            double lat = cursor.getDouble(index);
            index = cursor.getColumnIndexOrThrow("Longitude");
            double lng= cursor.getDouble(index);
            Location.distanceBetween(user_lat,user_lng, lat, lng,results);
            if(results[0]<1000){
                selectQuery = "SELECT * FROM carparks WHERE Latitude = "+lat+ " AND Longitude = "+lng;
                Cursor cursor1 = mDefaultWritableDatabase.rawQuery(selectQuery,null);
                cursor1.moveToFirst();
                String number = cursor1.getString(cursor1.getColumnIndexOrThrow("car_park_no"));
                String address = cursor1.getString(cursor1.getColumnIndexOrThrow("address"));
                double latitude = cursor1.getDouble(cursor1.getColumnIndexOrThrow("Latitude"));
                double longitude = cursor1.getDouble(cursor1.getColumnIndexOrThrow("Longitude"));
                String system_type = cursor1.getString(cursor1.getColumnIndexOrThrow("type_of_parking_system"));
                String car_park_type = cursor1.getString(cursor1.getColumnIndexOrThrow("car_park_type"));
                String free = cursor1.getString(cursor1.getColumnIndexOrThrow("free_parking"));
                String term = cursor1.getString(cursor1.getColumnIndexOrThrow("short_term_parking"));
                String night = cursor1.getString(cursor1.getColumnIndexOrThrow("night_parking"));
                int decks = cursor1.getInt(cursor1.getColumnIndexOrThrow("car_park_decks"));
                double height  = cursor1.getDouble(cursor1.getColumnIndexOrThrow("gantry_height"));
                String basement = cursor1.getString(cursor1.getColumnIndexOrThrow("car_park_basement"));
                boolean val1 = false;
                if( basement == "Y"){
                    val1 = true;
                }
                CarPark carpark = new CarPark(number, address, latitude,longitude,car_park_type, system_type,term, night,free, decks, height,val1);
                nearbyCarParks.add(carpark);
            }


        }
        return nearbyCarParks;
    }

    protected void readXLS(SQLiteDatabase db,String path, String tableName){
        //SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ tableName);
        db.beginTransaction();
        try {
            AssetManager manager = context.getAssets();
            InputStream file  = manager.open(path);

            HSSFWorkbook myWorkBook = new HSSFWorkbook(file);
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);
            Iterator<Row> rowIterator = mySheet.rowIterator();
            ContentValues contentvalues = new ContentValues();
            int rowno = 0;

            while(rowIterator.hasNext()){
                HSSFRow myRow = (HSSFRow) rowIterator.next();
                if(rowno != 0){
                    Iterator<Cell> cellIterator = myRow.cellIterator();
                    int colno = 0;
                    while( cellIterator.hasNext()){
                        HSSFCell myCell = (HSSFCell) cellIterator.next();
                        switch (colno) {
                            case 0:
                                contentvalues.put("car_park_no", myCell.toString());
                                break;
                            case 1:
                                contentvalues.put("address", myCell.toString());
                                break;
                            case 2:
                                contentvalues.put("Latitude", Double.parseDouble(myCell.toString()));
                                break;
                            case 3:
                                contentvalues.put("Longitude", Double.parseDouble(myCell.toString()));
                                break;
                            case 4:
                                contentvalues.put("car_park_type", myCell.toString());
                                break;
                            case 5:
                                contentvalues.put("type_of_parking_system", myCell.toString());
                                break;
                            case 6:
                                contentvalues.put("short_term_parking", myCell.toString());
                                break;
                            case 7:
                                contentvalues.put("free_parking", myCell.toString());
                                break;
                            case 8:
                                contentvalues.put("night_parking", myCell.toString());
                                break;
                            case 9:
                                contentvalues.put("car_park_decks", Double.parseDouble(myCell.toString()));
                                break;
                            case 10:
                                contentvalues.put("gantry_height", Double.parseDouble(myCell.toString()));
                                break;
                            case 11:
                                contentvalues.put("car_park_basement", myCell.toString());
                                break;


                        }
                        colno++;
                    }
                    db.insert(tableName, null, contentvalues);
                }
                rowno++;
            }

        } catch (IOException e) {
            if (db.inTransaction())
                db.endTransaction();

            e.printStackTrace();
        }
        finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    protected void carParkAvailability(JSONArray response, String tableName){
        //SQLiteDatabase db = this.getWritableDatabase();
        mDefaultWritableDatabase.execSQL("delete from " + tableName);
        ContentValues contentValues = new ContentValues();
        mDefaultWritableDatabase.beginTransaction();
        for(int i = 0; i<response.length();i++){
            JSONObject carParkData = null;
            try {
                carParkData = (JSONObject) response.get(i);
                String carParkNumber = carParkData.get("carpark_number").toString();
                JSONObject info = (JSONObject) carParkData.getJSONArray("carpark_info").get(0);
                int lots = Integer.parseInt(info.get("total_lots").toString());
                String type = info.get("lot_type").toString();
                int lotsAvail = Integer.parseInt(info.get("lots_available").toString());
                contentValues.put("car_park_no", carParkNumber);
                contentValues.put("total_lots", lots);
                contentValues.put("lots_available", lotsAvail);
                contentValues.put("lot_type", type);
                mDefaultWritableDatabase.insert(tableName, null, contentValues);
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
        mDefaultWritableDatabase.setTransactionSuccessful();
        mDefaultWritableDatabase.endTransaction();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        final SQLiteDatabase db;
        if(mDefaultWritableDatabase != null){
            db = mDefaultWritableDatabase;
        } else {
            db = super.getWritableDatabase();
        }
        return db;
    }

}
