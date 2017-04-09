package com.example.pocketbabytracker;

import android.content.Context;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

// use DB Browser for SQLite for creating db and tables and sample data
// add assets folder to main
// put pocketbaby.db in there

public class Database extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "pocketbaby.db";
    private static final int DATABASE_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        Log.d("Andrea", "database constructed with context");
    }
}
