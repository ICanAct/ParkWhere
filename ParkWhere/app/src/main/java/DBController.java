import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DBController extends SQLiteOpenHelper {

    public DBController(Context applicationcontext){
        super(applicationcontext, "CarparkDB.db", null, 1);
        // DATABASE is being created.
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String query;
        query = "CREATE TABLE IF NOT EXISTS carparks (car_park_no INTEGER PRIMARY KEY, address TEXT, Latitude DOUBLE, Longitude DOUBLE, " +
                "car_park_type TEXT, type_of_parking_system TEXT, short_term_parking TEXT, free_parking TEXT, night_parking TEXT, car_park_decks INTEGER, gantry_height DOUBLE, car_park_basement TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query;
        query = "DROP TABLE IF EXISTS proinfo"; // UPDATE THE QUERY STATEMENT
        db.execSQL(query);
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

    public void readCSV(String path, String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ tableName);
        try {
            FileReader file = new FileReader(path);
            BufferedReader buffer = new BufferedReader(file);
            ContentValues contentvalues = new ContentValues();
            String line;
            while((line = buffer.readLine()) != null){
                String[] str = line.split(",", 12);
                contentvalues.put("car_park_no", str[0]);
                contentvalues.put("address", str[1]);
                contentvalues.put("Latitude", Double.parseDouble(str[2]));
                contentvalues.put("Longitude", Double.parseDouble(str[3]));
                contentvalues.put("car_park_type", str[4]);
                contentvalues.put("type_of_parking_system", str[5]);
                contentvalues.put("short_term_parking", str[6]);
                contentvalues.put("free_parking", str[7]);
                contentvalues.put("night_parking", str[8]);
                contentvalues.put("car_park_decks", Integer.valueOf(str[9]));
                contentvalues.put("gantry_height", Double.parseDouble(str[9]));
                contentvalues.put("car_park_basement", str[11]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
