package com.nietky.librarythingbrowser;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

public class SearchHandler {
    private static final String TAG = "SearchHandler";
    
    private SharedPreferences sharedPref;
    
    private Context _context;
    private ArrayList<Integer> _ids;

    public DbHelperNew dbHelper;

    public SearchHandler(Context context) {
        String METHOD = ".constructor(Context): ";
        Log.d(TAG + METHOD, "start");
        
        _context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(_context.getApplicationContext());
        dbHelper = new DbHelperNew(_context.getApplicationContext());
        dbHelper.open();
        
        _ids = new ArrayList<Integer> ();
    }
    
    public void setIds () {
        String METHOD = "-setIds() [all]";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        _ids = dbHelper.getAllIds();
    }
    
    public void setIds (String commaSeparatedIds) {
        String METHOD = "-setIds(a String)";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        String[] ids = commaSeparatedIds.split(",");
        _ids = new ArrayList<Integer> ();
        for (int i = 0; i < ids.length; i++) {
            int id = Integer.valueOf(ids[i]);
            _ids.add(id);
        }
    }
    
    public void setIds(ArrayList<Integer> ids) {
        String METHOD = "-setIds(ArrayList<Integer> ids.size()=" + ids.size() + ")";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        _ids = ids;
    }
    
    public ArrayList<Integer> getIds() {
        String METHOD = ".getIds()";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        return _ids;
    }
    
    public String getString () {
        String METHOD = ".getString()";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        String s = "";
        for (int i = 0; i < _ids.size(); i++) {
            s += String.valueOf(_ids.get(i)) + ",";
        }
        return s;
    }
    
    public Cursor getCursor() {
        String METHOD = ".getCursor(): ";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        
        return dbHelper.getIds(_ids);
    }
    
    private String transform (String original) {
        if (sharedPref.getBoolean("case_sensitive_search", false))
            return original;
        else
            return original.toLowerCase();
    }

    public void restrictByQuery(String query) {
        String METHOD = "-restrictByQuery(" + query + "): ";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            int nCols = currentCursor.getColumnCount();
            int idColN = currentCursor.getColumnIndex("_id");
            boolean queryMatch = false;
            for (int iCol = 0; iCol < nCols; iCol++) {
                if (iCol == idColN) continue;
                String fieldValue = currentCursor.getString(iCol);
                if (transform(fieldValue).contains(transform(query))) {
                    queryMatch = true;
                    break;
                }
            }
            if (!queryMatch) {
                _ids.remove((Object) currentCursor.getInt(currentCursor.getColumnIndex("_id")));
            }
            currentCursor.moveToNext();
        }
    }
    
    public void restrictByTag(String tag) {
        String METHOD = "-restrictByTag(" + tag + "): ";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            String rawTags = currentCursor.getString(currentCursor.getColumnIndex("tags"));
            String [] tags = rawTags.split(",");
            boolean tagMatch = false;
            for (int i = 0; i < tags.length; i++) {
                if (tag.equals(tags[i].trim())) {
                    tagMatch = true;
                    break;
                }
            }
            if (!tagMatch) {
                _ids.remove((Object) currentCursor.getInt(currentCursor.getColumnIndex("_id")));
            }
            currentCursor.moveToNext();
        }
    }
    
    public void restrictByCollection(String collection) {
        String METHOD = "-restrictByCollection(" + collection + "):";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            String rawCollections = currentCursor.getString(currentCursor.getColumnIndex("collections"));
            Log.d(TAG + METHOD, "rawCollections=" + rawCollections);
            String [] collections = rawCollections.split(",");
            boolean collectionMatch = false;
            for (int i = 0; i < collections.length; i++) {
                if (collection.equals(collections[i].trim())) {
                    collectionMatch = true;
                    Log.d(TAG + METHOD, "match found for collections[" + i + "]=" + collections[i].trim());
                    break;
                }
            }
            if (!collectionMatch) {
                Log.d(TAG + METHOD, "about to remove (_ids.size()=" + _ids.size() + ")");
                _ids.remove((Object) currentCursor.getInt(currentCursor.getColumnIndex("_id")));
                Log.d(TAG + METHOD, "removed (_ids.size()=" + _ids.size() + ")");
            }
            currentCursor.moveToNext();
        }
    }
    
    public void restrictByAuthor (String author) {
        String METHOD = "-restrictByAuthor(" + author + "):";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            String author1 = currentCursor.getString(currentCursor.getColumnIndex("author1"));
            String author2 = currentCursor.getString(currentCursor.getColumnIndex("author2"));
            String author_other = currentCursor.getString(currentCursor.getColumnIndex("author_other"));
            String authors = author1 + " " + author2 + " " + author_other;
//            Log.d(TAG + METHOD, "authors=" + authors);
            if (!authors.contains(author)) {
                _ids.remove((Object) currentCursor.getInt(currentCursor.getColumnIndex("_id")));
            }
            currentCursor.moveToNext();
        }
    }
    
    public void restrictByReview () {
        String METHOD = "-restrictByReview(): ";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            String review = currentCursor.getString(currentCursor.getColumnIndex("review"));
            if (review.trim().length() == 0) {
                _ids.remove((Object) currentCursor.getInt(currentCursor.getColumnIndex("_id")));
            }
            currentCursor.moveToNext();
        }
    }
    public void restrictByComments () {
        String METHOD = "-restrictByComments(): ";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            String comments = currentCursor.getString(currentCursor.getColumnIndex("comments"));
            String comments_private = currentCursor.getString(currentCursor.getColumnIndex("comments_private"));
            if (comments.trim().length() == 0 && comments_private.trim().length() == 0) {
                _ids.remove((Object) currentCursor.getInt(currentCursor.getColumnIndex("_id")));
            }
            currentCursor.moveToNext();
        }
    }
    
    
    public void close() {
        dbHelper.close();
    }
    
}
