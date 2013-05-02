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

public class NotesDbHelper extends SQLiteOpenHelper {
    String TAG = "NotesDbHelper";
    SharedPreferences sharedPref;
    LogHandler logger;
    
    static int DATABASE_VERSION = 1;
    static String DATABASE_NAME = "LibraryThingBrowserNotes2";
    
    String DATABASE_PATH = "";
    SQLiteDatabase Db;
    String TABLE = "notes_table";
    Context context;
    String[] KEYS = {"book_id", "notes" };

    public NotesDbHelper(Context contextLocal) {      
        super(contextLocal, DATABASE_NAME, null, DATABASE_VERSION);
        String METHOD = ".constructor: ";
        Log.d(TAG + METHOD, "start");
        
        sharedPref = PreferenceManager.getDefaultSharedPreferences(contextLocal.getApplicationContext());
        logger = new LogHandler(sharedPref);
        logger.log(TAG + METHOD, "Start");
        
        context = contextLocal;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(contextLocal
                .getApplicationContext());
        Log.d(TAG, "Helper ready, looking at " + DATABASE_PATH);
    }

    public void onCreate(SQLiteDatabase db) {
        String METHOD = ".onCreate: ";
        Log.d(TAG + METHOD, "start");
        
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE + "("
                + "book_id INTEGER PRIMARY KEY, note TEXT)";
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
        String METHOD = ".open()";
        Log.d(TAG + METHOD, "start");
        
        String path = DATABASE_PATH + DATABASE_NAME;
        Db = this.getWritableDatabase();
        return Db != null;
    }

//    public void addRow(String[] values) {
//        String METHOD = ".addRow: ";
////        Log.d(TAG + METHOD, "start");
//        
////        Log.d(TAG, KEYS.length + " key labels; " + values.length + " values.");
//        ContentValues cvalues = new ContentValues();
//        for (int i = 0; i < values.length; i++)
//            cvalues.put(KEYS[i], values[i]);
//        Db.insert(TABLE, null, cvalues);
//    }

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
    
    public String getNote(Integer book_id) {
        String METHOD = ".getNote(book_id=" + book_id + ")";
        Log.d(TAG + METHOD, "Start");
        String sql = "SELECT * FROM " + TABLE + " WHERE book_id=" + book_id;
        Log.d(TAG + METHOD, "SQL=" + sql);
        Cursor cursor = Db.rawQuery(sql, null);
        String note = "NONE";
        if (cursor != null)
            cursor.moveToFirst();
        Log.d(TAG + METHOD, "cursor length=" + cursor.getCount());
        if (!cursor.isAfterLast())
            note = cursor.getString(cursor.getColumnIndex("note"));
        return note;
    }

    public long setNote (Integer book_id, String note) {
        String existingNote = getNote(book_id); 
        String METHOD = ".setNote(book_id=" + book_id + ", note=" + note + ")";
        ContentValues values = new ContentValues();
        values.put("book_id", book_id.toString());
        values.put("note", note.toString());
        Log.d(TAG + METHOD, "contentValues.toString()=" + values.toString());
        long result = -2;
        if (existingNote.matches("NONE"))
            result = Db.insert(TABLE, null, values);
        else
            result = Db.replace(TABLE, null, values);
        return result;
        
    }
}
