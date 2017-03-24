package net.lennartolsen.blescanner;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;


/**
 * Created by gkevi on 3/23/2017.
 */

public class MyDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "devices.db";
    public static final String TABLE_DEVICES = "devices";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DEVICENAME = "deviceName";
    public static final String COLUMN_ROOMNUMBER = "roomNumber";

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_DEVICES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " +
                COLUMN_DEVICENAME + " TEXT " +
                COLUMN_ROOMNUMBER + " TEXT " +
                ");";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        onCreate(db);
    }

    // Add new row to the database
    public void addDevice(Devices device){
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICENAME, device.getDeviceName());
        values.put(COLUMN_ROOMNUMBER, device.getRoomNumber());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_DEVICES, null, values);
        db.close();
    }



    public boolean checkIfEmpty(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + TABLE_DEVICES, null);
        boolean empty = false;
        if(mCursor.getCount() == 0){
            empty = true;
        }
        return empty;
    }


    public String getRoomNumberOfDevice(String aDeviceName){
        String roomNumberResult = "";
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DEVICES + " WHERE " + COLUMN_DEVICENAME + "=\"" +
                aDeviceName + "\"";

        // Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        // Move to the first row in your results
        c.moveToFirst();

        while(!c.isAfterLast()){
            if(c.getString(c.getColumnIndex("roomNumber")) != null){
                roomNumberResult = c.getString(c.getColumnIndex("roomNumber"));
            }
        }
        db.close();
        return roomNumberResult;

    }

    // Delete a device from db
    public void deleteDevice(String deviceName){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_DEVICES + " WHERE " + COLUMN_DEVICENAME + "=\"" +
        deviceName + "\"");
    }

    // Print out the database as a string
    public String databaseToString(){
        String dbString = "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_DEVICES + " WHERE 1";

        // Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        // Move to the first row in your results
        c.moveToFirst();

        while(!c.isAfterLast()){
            if(c.getString(c.getColumnIndex("deviceName")) != null){
                dbString += c.getString(c.getColumnIndex("deviceName"));
                dbString += "\n";
            }
        }
        db.close();
        return dbString;
    }

}
