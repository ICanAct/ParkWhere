package com.example.parkwhere;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
import java.util.Iterator;


public class DBController extends SQLiteOpenHelper {
    private Context context;

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
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query;
        query = "DROP TABLE IF EXISTS carparks"; // UPDATE THE QUERY STATEMENT
        db.execSQL(query);
        db.execSQL("DROP TABLE IF EXISTS availability");

        onCreate(db);

    }

    public Cursor getCarparks(LatLng latLng){
        double lat = latLng.latitude;        // FOR the query part please change accordingly
        double lng = latLng.longitude;       // to the radius calculated.
        String selectQuery = "SELECT * FROM carparks WHERE latitude< lat OR longitude < lng";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor;
    }

    protected void readXLS(String path, String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
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
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + tableName);
        ContentValues contentValues = new ContentValues();
        db.beginTransaction();
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
                db.insert(tableName, null, contentValues);
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

}
