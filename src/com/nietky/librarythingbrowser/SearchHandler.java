package com.nietky.librarythingbrowser;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

public class SearchHandler {
    String TAG = "SearchHandler";
    SharedPreferences sharedPref;
    LogHandler logger;
    
    Context _context;
    ArrayList<Integer> _ids;

    DbHelperNew dbHelper;

    public SearchHandler(Context context) {
        String METHOD = ".constructor(Context): ";
        _context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(_context);
        logger = new LogHandler(sharedPref);
        logger.log(TAG + METHOD, "Start");
       
        _ids = new ArrayList<Integer> ();
    }
    
    public void openDbHelper() {
        dbHelper = new DbHelperNew(_context);
        dbHelper.open();
    }
    
    public void closeDbHelper() {
        dbHelper.close();
    }
    
    public void setIds () {
        String METHOD = "-setIds() [all]";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        openDbHelper();
        _ids = dbHelper.getAllIds();
        closeDbHelper();
    }
    
    public void setIds (String commaSeparatedIds) {
        String METHOD = "-setIds(a String)";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        Log.d(TAG + METHOD, "commaSeparatedIds=" + commaSeparatedIds);
        String[] ids = commaSeparatedIds.split(",");
        _ids = new ArrayList<Integer> ();
        Log.d(TAG + METHOD, "ids.length=" + ids.length);
        if (ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                if (ids[i].length() > 0) {
                    int id = Integer.valueOf(ids[i]);
                    _ids.add(id);
                }
            }
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
        openDbHelper();
        return dbHelper.getIds(_ids);
    }
    
    public int size() {
        return _ids.size();
    }
    
    public Integer getId(int position) {
        return _ids.get(position);
    }
    
    public ArrayList<String> getColumnArray(String columnName) {
        String METHOD = ".getColumnArray(columnName=" + columnName + ")";
        ArrayList<String> column = new ArrayList<String>();
        openDbHelper();
        Cursor cursor = dbHelper.getColumn(_ids, columnName);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            column.add(cursor.getString(0));
            cursor.moveToNext();
        }
        close();
        return column;
    }
    
    public HashMap<String, String> getFields(Integer _id) {
        String METHOD = ".getFields(_id=" + _id + "): ";
        Log.d(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        
        String fieldname;
        String[] fieldnames = { "_id", "book_id", "title", "author1",
                "author2", "author_other", "publication", "date", "ISBNs",
                "series", "source", "lang1", "lang2", "lang_orig", "LCC",
                "DDC", "bookcrossing", "date_entered", "date_acquired",
                "date_started", "date_ended", "stars", "collections", "tags",
                "review", "summary", "comments", "comments_private", "copies",
                "encoding" };
        HashMap<String, String> fields = new HashMap<String, String>();
        for (int i = 0; i < fieldnames.length; i += 1)
            fields.put(fieldnames[i], "");
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            if (currentCursor.getInt(currentCursor.getColumnIndex("_id")) == _id) {
                for (int i = 0; i < fieldnames.length; i += 1) {
                    fieldname = fieldnames[i];
                    int index = currentCursor.getColumnIndex(fieldname);
                    String content = "";
                    if (index > -1)
                        content = currentCursor.getString(index);
                    content = content.replace("[return]", "\n");
                    content = content.trim();
                    fields.put(fieldname, content);
                }
                break;
            }
        }
        currentCursor.close();
        close();
        return fields;
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
        tag = tag.trim();
        Log.d(TAG + METHOD, "trimmed tag query to |" + tag + "|");
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
        collection = collection.trim();
        Log.d(TAG + METHOD, "trimmed collection query to |" + collection + "|");
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
        author = author.trim();
        Log.d(TAG + METHOD, "trimmed author query to |" + author + "|");
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
