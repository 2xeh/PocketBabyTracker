package com.example.pocketbabytracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

// manage the database connection
public class DatabaseObject {

    private static Database dbHelper;
    private SQLiteDatabase db;

    public DatabaseObject(Context context) {
        Log.d("Andrea", "creating database object");

        dbHelper = new Database(context);
        this.dbHelper.getWritableDatabase();
        this.db = dbHelper.getReadableDatabase();

        Log.d("Andrea", "DatabaseObject constructed, have dbHelper");
    }

    public SQLiteDatabase getDbConnection(){
        return this.db;
    }

    public void closeDbConnection(){
        if(this.db != null){
            this.db.close();
        }
    }
}