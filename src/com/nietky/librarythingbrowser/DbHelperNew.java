package com.nietky.librarythingbrowser;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

public class DbHelperNew extends SQLiteOpenHelper {
    String TAG = "DbHelperNew";
    SharedPreferences sharedPref;
    LogHandler logger;
    
    static int DATABASE_VERSION = 1;
    static String DATABASE_NAME = "LibraryThing";
    
    String DATABASE_PATH = "";
    SQLiteDatabase Db;
    String TABLE = "books";
    Context context;
    String[] KEYS = { "book_id", "title", "author1",
            "author2", "author_other", "publication", "date", "ISBNs",
            "series", "source", "lang1", "lang2", "lang_orig", "LCC", "DDC",
            "bookcrossing", "date_entered", "date_acquired", "date_started",
            "date_ended", "stars", "collections", "tags", "review", "summary",
            "comments", "comments_private", "copies", "encoding" };
    String[] ALL_KEYS = { "_id", "book_id", "title", "author1",
            "author2", "author_other", "publication", "date", "ISBNs",
            "series", "source", "lang1", "lang2", "lang_orig", "LCC", "DDC",
            "bookcrossing", "date_entered", "date_acquired", "date_started",
            "date_ended", "stars", "collections", "tags", "review", "summary",
            "comments", "comments_private", "copies", "encoding" };

    public DbHelperNew(Context contextLocal) {      
        super(contextLocal, DATABASE_NAME, null, DATABASE_VERSION);
        String METHOD = ".constructor: ";
        Log.d(TAG + METHOD, "start");
        
        sharedPref = PreferenceManager.getDefaultSharedPreferences(contextLocal.getApplicationContext());
        logger = new LogHandler(sharedPref);
        logger.log(TAG + METHOD, "Start");
        
        DATABASE_PATH = "/data/data/" + contextLocal.getPackageName()
                + "/databases/";
        context = contextLocal;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(contextLocal
                .getApplicationContext());
        Log.d(TAG, "Helper ready, looking at " + DATABASE_PATH);
    }

    public void onCreate(SQLiteDatabase db) {
        String METHOD = ".onCreate: ";
        Log.d(TAG + METHOD, "start");
        
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE + "("
                + "_id INTEGER PRIMARY KEY" + ", book_id TEXT" + ", title TEXT"
                + ", author1 TEXT" + ", author2 TEXT" + ", author_other TEXT"
                + ", publication TEXT" + ", date TEXT" + ", ISBNs TEXT"
                + ", series TEXT" + ", source TEXT" + ", lang1 TEXT"
                + ", lang2 TEXT" + ", lang_orig TEXT" + ", LCC TEXT"
                + ", DDC TEXT" + ", bookcrossing TEXT" + ", date_entered TEXT"
                + ", date_acquired TEXT" + ", date_started TEXT"
                + ", date_ended TEXT" + ", stars TEXT" + ", collections TEXT"
                + ", tags TEXT" + ", review TEXT" + ", summary TEXT"
                + ", comments TEXT" + ", comments_private TEXT"
                + ", copies TEXT" + ", encoding TEXT" + ")";
        Log.d(TAG, "onCreate, running SQL: " + CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String METHOD = ".onUpgrade: ";
        Log.d(TAG + METHOD, "start");
        
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    @SuppressWarnings("unused")
    public boolean open() throws SQLException {
        String METHOD = ".open: ";
        Log.d(TAG + METHOD, "start");
        
        String path = DATABASE_PATH + DATABASE_NAME;
        Db = this.getWritableDatabase();
        return Db != null;
    }

    public void addRow(String[] values) {
        String METHOD = ".addRow: ";
//        Log.d(TAG + METHOD, "start");
        
//        Log.d(TAG, KEYS.length + " key labels; " + values.length + " values.");
        ContentValues cvalues = new ContentValues();
        for (int i = 0; i < values.length; i++)
            cvalues.put(KEYS[i], values[i]);
        Db.insert(TABLE, null, cvalues);
    }

    public void close() {
        String METHOD = ".close: ";
        Log.d(TAG + METHOD, "start");
        
        Db.close();
    }

    public void delete() {
        String METHOD = ".delete: ";
        Log.d(TAG + METHOD, "start");
        
        Db = this.getWritableDatabase();
        Db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(Db);
    }
    
    public Cursor getIds(ArrayList<Integer> ids) {
        return getIds(ids, "_id");
    }
    
    public Cursor getIds(ArrayList<Integer> ids, String sortOrder) {
        String METHOD = ".getIds: ";
        Log.d(TAG + METHOD, "start");
        
        if (ids.size() == 0) {
            return Db.rawQuery("SELECT * FROM " + TABLE + " WHERE _id == -1", null);
        } else { 
            try {
                String sql = "SELECT * FROM " + TABLE + " WHERE ";
                sql += "_id IN (" + ids.get(0);
                for (int i = 1; i < ids.size(); i++) {
                    sql += ", " + ids.get(i);
                }
                sql = sql + ") ORDER BY " + sortOrder;
                Cursor cursor = Db.rawQuery(sql, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                }
                Log.d(TAG + METHOD, "sql=" + sql);
                Log.d(TAG + METHOD, "returning a Cursor with " + cursor.getCount() + " from a request for " + ids.size() + " ids");
                return cursor;
            } catch (SQLException mSQLException) {
                Log.e(TAG, "getTestData >>" + mSQLException.toString());
                throw mSQLException;
            }
        }
    }
    
    public Cursor getColumn(ArrayList<Integer> ids, String columnName) {
        return getColumn(ids, columnName, "_id");
    }
    
    public Cursor getColumn(ArrayList<Integer> ids, String columnName, String sortOrder) {
        String METHOD = ".getColumn(" + ids.size() + " ids, columnName=" + columnName + ")";
        if (ids.size() == 0) {
            return Db.rawQuery("SELECT " + columnName + " FROM " + TABLE + " WHERE _id == -1", null);
        } else { 
            try {
                String sql = "SELECT " + columnName + " FROM " + TABLE + " WHERE ";
                sql += "_id IN (" + ids.get(0);
                for (int i = 1; i < ids.size(); i++) {
                    sql += ", " + ids.get(i);
                }
                sql += ") ORDER BY " + sortOrder;
                Cursor cursor = Db.rawQuery(sql, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                }
                Log.d(TAG + METHOD, "returning a Cursor with " + cursor.getCount() + " from a request for " + ids.size() + " ids");
                return cursor;
            } catch (SQLException mSQLException) {
                Log.e(TAG, "getTestData >>" + mSQLException.toString());
                throw mSQLException;
            }
        }
    }
    
    public ArrayList<Integer> getAllIds() {
        String METHOD = ".getAllIds: ";
        Log.d(TAG + METHOD, "start");
        
        String sql = "SELECT _id FROM " + TABLE;
        Cursor cursor = Db.rawQuery(sql, null);
        if (cursor != null) {
            cursor.moveToNext();
        }
        cursor.moveToFirst();
        ArrayList<Integer> _ids = new ArrayList<Integer>();
        while (!cursor.isAfterLast()) {
            _ids.add(cursor.getInt(cursor.getColumnIndex("_id")));
            cursor.moveToNext();
        }
        Log.d(TAG + METHOD, "returning " + _ids.size() + " ids");
        return _ids;
    }
    
    public Cursor searchTag(String search, String orderByColumn) {
        String METHOD = ".searchTag: ";
        Log.d(TAG + METHOD, "start");
        
        try {
            String sql = "SELECT * FROM " + TABLE + " WHERE ";
            sql += "tags LIKE '%" + search + "%'";
            sql += " ORDER BY " + orderByColumn;
            Cursor cursor = Db.rawQuery(sql, null);
            if (cursor != null) {
                cursor.moveToNext();
            }
            return cursor;
        } catch (SQLException mSQLException) {
            Log.e(TAG, "getTestData >>" + mSQLException.toString());
            throw mSQLException;
        }
    }
    
    public Cursor getRow(String id) {
        String METHOD = ".getRow(id=" + id + "): ";
        Log.d(TAG + METHOD, "start");
        
        String sql = "SELECT * FROM " + TABLE + " WHERE _id='" + id + "'";
        Cursor cursor = Db.rawQuery(sql, null);
        return cursor;
    }

}
