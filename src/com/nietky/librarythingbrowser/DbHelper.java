package com.nietky.librarythingbrowser;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
    private static String TAG = "DbHelper";
    private SharedPreferences sharedPref;
    
    private static int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "LibraryThingBrowser11";
    
    public SQLiteDatabase db;
    private String TABLE = "ltb";
    private Context context;
    
    public String[] KEYS = {"_id",
            "bookID",
            "title",
            "authorLastFirst",
            "authorFirstLast",
            "authorOthers",
            "publication",
            "date",
            "ISBN",
            "series",
            "source",
            "language1",
            "language2",
            "language3",
            "LCC",
            "DDC",
            "bookCrossingID",
            "dateEntered",
            "dateAcquired",
            "dateStarted",
            "dateFinished",
            "stars",
            "collections",
            "tags",
            "review",
            "comments",
            "commentsPrivate",
            "copies",
            "encoding"};
    
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        String METHOD = ".DbHelper constructor()";
        Log.d(TAG + METHOD, "Start");
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public void onCreate(SQLiteDatabase db) {
        String METHOD = ".onCreate()";
        String SQL = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( ";
        for (int i = 0; i < KEYS.length; i++) {
            if (i == 0)
                SQL += KEYS[i] + " INTEGER PRIMARY KEY AUTOINCREMENT";
            else
                SQL += KEYS[i] + " TEXT";
            if (i < KEYS.length - 1)
                SQL += ", ";
            else
                SQL += " );";
        }
        db.execSQL(SQL);
        Log.d(TAG + METHOD, SQL);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String METHOD = ".onUpgrade()";
        String SQL = "DROP TABLE IF EXISTS " + TABLE;
        db.execSQL(SQL);
        Log.d(TAG + METHOD, SQL);
        onCreate(db);
    }
    
    public void open() throws SQLException {
        String METHOD = ".open()";
        Log.d(TAG + METHOD, "Start");
        this.db = this.getWritableDatabase();
    }

    public void addBook (Book book) {
        open();
        db.insert(TABLE, null, book.getContentValues());
        close();
    }
    
    public void addBookFromStringArray (String[] values) {
        open();
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < KEYS.length; i++) {
            contentValues.put(KEYS[i], values[i]);
        }
        db.insert(TABLE, null, contentValues);
        close();
    }
    
    public void addBookFromStringArrayDbUnopened (String[] values) {
        ContentValues cv = new ContentValues();
        for (int i = 0; i < KEYS.length; i++) {
            cv.put(KEYS[i], values[i]);
        }
        db.insert(TABLE, null, cv);
    }
    
    public void addBooks (List<Book> books) {
        open();
        db.beginTransaction();
        try {
            for (int i = 0; i < books.size(); i++)
                db.insert(TABLE, null, books.get(i).getContentValues());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        close();
    }
    
    public void addBooksFromStringArrays (List<String[]> values) {
        // The first value in each String[] WILL BE IGNORED.
        open();
        db.beginTransaction();
        try {
            for (int i = 0; i < values.size(); i++) {
                ContentValues contentValues = new ContentValues();
                for (int j = 1; j < KEYS.length; j++)
                    contentValues.put(KEYS[j], values.get(i)[j]);
                db.insert(TABLE, null, contentValues);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        close();
    }
    
    public void deleteTable () {
        open();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        close();
    }
    
    public Book getBookFromDbID (int dbID) {
        Book book = new Book();
        open();
        String SQL = "SELECT * FROM " + TABLE + " WHERE _id == " + dbID;
        Cursor cursor = db.rawQuery(SQL, null);
        if (cursor != null)
            book = cursorToBook(cursor);
        close();
        return book;
    }
    
    public List<Book> getAllBooks () {
        List<Book> books = new ArrayList<Book>();
        open();
        String SQL = "SELECT * FROM " + TABLE;
        Cursor cursor = db.rawQuery(SQL, null);
        books = cursorToBooks(cursor);
        close();
        return books;
    }
    
    public List<Book> getBooksFromDbIDs (List<Integer> dbIDs) {
        List<Book> books = new ArrayList<Book>();
        String IDString = "_id IN (";
        for (int i = 0; i < dbIDs.size(); i++) {
            IDString += dbIDs.get(i).toString();
            if (i < dbIDs.size() - 1)
                IDString += ", ";
        }
        IDString += ")";
        open();
        String SQL = "SELECT * FROM " + TABLE + " WHERE " + IDString;
        Cursor cursor = db.rawQuery(SQL, null);
        books = cursorToBooks(cursor);
        close();
        return books;
    }
    
    public List<String> getColumn (String fieldName) {
        List<String> values = new ArrayList<String>();
        open();
        String SQL = "SELECT " + fieldName + " FROM " + TABLE;
        Cursor cursor = db.rawQuery(SQL, null);
        if (cursor != null)
            cursor.moveToFirst();
        else
            return values;
        while (!cursor.isAfterLast()) {
            values.add(cursor.getString(0));
            cursor.moveToNext();
        }
        return values;
    }
    
//    public List<Book> searchAllBooks (String query, String[] fieldNames) {
//        List<String> allDbIDStrings = getColumn("_id");
//        List<Integer> allDbIDs = new ArrayList<Integer>();
//        for (int i = 0; i < allDbIDStrings.size(); i++)
//            allDbIDs.add(Integer.valueOf(allDbIDStrings.get(i)));
//        return searchBooks(allDbIDs, query, fieldNames);
//    }
//    
//    public List<Book> searchBooks (List<Integer> dbIDs, String query, String[] fieldNames) {
//        // Unfinished
//        List<Book> books = new ArrayList<Book>();
//        open();
//        String SQL = "SELECT * FROM " + TABLE + " WHERE " + fieldName;
//        Cursor cursor = db.rawQuery(SQL, null);
//        books = cursorToBooks(cursor);
//        close();
//        return books;
//    }
    
    private Book cursorToBook (Cursor cursor) {
        Book book = new Book();
        book.dbID = cursor.getInt(0);
        book.bookID = cursor.getInt(1);
        book.title = cursor.getString(2);
        book.authorLastFirst = cursor.getString(3);
        book.authorFirstLast = cursor.getString(4);
        book.authorOthers = cursor.getString(5);
        book.publication = cursor.getString(6);
        book.date = cursor.getString(7);
        book.ISBN = cursor.getString(8);
        book.series = cursor.getString(9);
        book.source = cursor.getString(10);
        book.language1 = cursor.getString(11);
        book.language2 = cursor.getString(12);
        book.language3 = cursor.getString(13);
        book.LCC = cursor.getString(14);
        book.DDC = cursor.getString(15);
        book.bookCrossingID = cursor.getString(16);
        book.dateEntered = Book.parseDate(cursor.getString(17));
        book.dateAcquired = Book.parseDate(cursor.getString(18));
        book.dateStarted = Book.parseDate(cursor.getString(19));
        book.dateFinished = Book.parseDate(cursor.getString(20));
        book.stars = cursor.getFloat(21);
        book.collections = cursor.getString(22).split(",");
        book.tags = cursor.getString(23).split(",");
        book.review = cursor.getString(24);
        book.comments = cursor.getString(25);
        book.commentsPrivate = cursor.getString(26);
        book.copies = cursor.getInt(27);
        book.encoding = cursor.getString(28);
        return book;
    }
    
    private List<Book> cursorToBooks (Cursor cursor) {
        List<Book> books = new ArrayList<Book>();
        if (cursor != null)
            cursor.moveToFirst();
        else
            return books;
        while (!cursor.isAfterLast()) {
            books.add(cursorToBook(cursor));
            cursor.moveToNext();
        }
        return books;
    }
}
