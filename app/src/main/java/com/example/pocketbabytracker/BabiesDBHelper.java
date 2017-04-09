package com.example.pocketbabytracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Create the db per this video (2:28):
 * https://www.youtube.com/watch?v=8jspmT4KAQw
 *
 * This video was very helpful:
 * https://www.youtube.com/watch?v=n0LEDTV_v44
 */

/**
 * in the calling code, you'll need to run something like this in onCreate:
 * 1. instantiate this helper class
 * 2. SQLiteDatabase db = BabiesDBHelper.getReadableDatabase();
 */

public class BabiesDBHelper extends SQLiteOpenHelper {

    static final String DB_NAME = "pocketbaby";
    static final String DB_VERSION = "1";

    static final String ID_COLUMN = "_id";

    // DB Table and column names
    static final String BABIES_TABLE = "babies";
    static final String BABIES_TABLE_BABY_NAMES_COL = "baby_name";
    static final String BABIES_TABLE_BIRTHDAY_COL = "birthday";
    static final String BABIES_TABLE_GENDER_COL = "gender";
    static final String BABIES_TABLE_NEW_COL = "NEW";

    public BabiesDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + BABIES_TABLE
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BABIES_TABLE_BABY_NAMES_COL + " TEXT, "
                + BABIES_TABLE_GENDER_COL + " TEXT, "
                + BABIES_TABLE_BIRTHDAY_COL + " TEXT);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 1){

            db.execSQL("CREATE TABLE " + BABIES_TABLE
                    + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + BABIES_TABLE_BABY_NAMES_COL + " TEXT, "
                    + BABIES_TABLE_GENDER_COL + " TEXT, "
                    + BABIES_TABLE_BIRTHDAY_COL + " TEXT);"
            );
        }
        // if needing to update....
        // per this video: https://www.youtube.com/watch?v=38h0pyFxNQE
//        else if(oldVersion < 2) {
//            db.execSQL("ALTER TABLE " + BABIES_TABLE
//                    + " ADD COLUMN "
//                    + BABIES_TABLE_NEW_COL + " TEXT);"
//            );
//        }
    }

    public Cursor getBabiesTableData(SQLiteDatabase db){
        // string array to define columns to be returned
        String[] columns = {
                ID_COLUMN,
                BABIES_TABLE_BABY_NAMES_COL,
                BABIES_TABLE_GENDER_COL,
                BABIES_TABLE_BIRTHDAY_COL
        };

        // define sort order
        String sortOrder = ID_COLUMN + " DESC";

        // query( tableName, columnsArray, selection, selectionArgs, groupBy, having, orderBy, limit)
        return db.query(BABIES_TABLE, columns, null, null, null, null, sortOrder);
    }
}
