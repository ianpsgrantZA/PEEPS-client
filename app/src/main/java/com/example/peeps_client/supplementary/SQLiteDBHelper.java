package com.example.peeps_client.supplementary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "peeps_local";
    public static final String LOCATION_TABLE_NAME = "saved_locations";
    public static final String LOCATION_COLUMN_ID = "id";
    public static final String LOCATION_COLUMN_NAME = "name";
    public static final String LOCATION_COLUMN_LAT = "lat";
    public static final String LOCATION_COLUMN_LONG = "long";
    public static final String LOCATION_COLUMN_IMAGE = "image";


    public SQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + LOCATION_TABLE_NAME + " (" +
                LOCATION_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LOCATION_COLUMN_NAME + " TEXT, " +
                LOCATION_COLUMN_LAT + " FLOAT, " +
                LOCATION_COLUMN_LONG + " FLOAT, " +
                LOCATION_COLUMN_IMAGE + " TEXT " + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}
