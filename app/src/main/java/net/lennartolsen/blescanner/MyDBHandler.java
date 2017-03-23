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
    public static final String COLUMN_ROOMNUMBER = "roomNumber";

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_DEVICES + "(" +
                COLUMN_ID + " " +
                COLUMN_ROOMNUMBER + " " +
                ");";
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
