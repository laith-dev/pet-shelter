package com.example.android.petsshelter.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.petsshelter.data.PetContract.PetEntry;

public class PetDbHelper extends SQLiteOpenHelper {

    public static final String TAG = PetDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "shelter.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_DROP_PETS_TABLE = "DROP TABLE IF EXISTS " + DATABASE_NAME;

    private static final String SQL_CREATE_PETS_TABLE =
            "CREATE TABLE " + PetEntry.TABLE_NAME + "(" +
                    PetEntry.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PetEntry.COL_PET_NAME + " TEXT NOT NULL, " +
                    PetEntry.COL_PET_BREED + " TEXT, " +
                    PetEntry.COL_PET_GENDER + " INTEGER NOT NULL, " +
                    PetEntry.COL_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0)";

    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to be done here.
    }
}
