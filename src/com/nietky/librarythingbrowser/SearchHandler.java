package com.nietky.librarythingbrowser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
    String _sortOrder = "_id";

    DbHelperNew dbHelper;

    public SearchHandler(Context context) {
        String METHOD = ".constructor(Context): ";
        _context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        logger = new LogHandler(sharedPref);
        logger.log(TAG + METHOD, "Start");
        _ids = new ArrayList<Integer> ();
    }
    
    public void openDbHelper() {
        dbHelper = new DbHelperNew(_context.getApplicationContext());
        dbHelper.open();
    }
    
    public void closeDbHelper() {
        dbHelper.close();
    }
    
    public void setIds () {
        String METHOD = "-setIds() [all]";
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        openDbHelper();
        _ids = dbHelper.getAllIds();
        closeDbHelper();
    }
    
    public void setIds (String commaSeparatedIds) {
        String METHOD = "-setIds(a String)";
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        logger.log(TAG + METHOD, "commaSeparatedIds=" + commaSeparatedIds);
        String[] ids = commaSeparatedIds.split(",");
        _ids = new ArrayList<Integer> ();
        logger.log(TAG + METHOD, "ids.length=" + ids.length);
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
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        _ids = ids;
    }
    
    public ArrayList<Integer> getIds() {
        String METHOD = ".getIds()";
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        return _ids;
    }
    
    public String getString () {
        String METHOD = ".getString()";
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        String s = "";
        for (int i = 0; i < _ids.size(); i++) {
            s += String.valueOf(_ids.get(i)) + ",";
        }
        return s;
    }
    
    public Cursor getCursor() {
        String METHOD = ".getCursor(): ";
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        
        //Log.i(TAG + METHOD, "performance_track getCursor_tag1");
        
        openDbHelper();
        
        //Log.i(TAG + METHOD, "performance_track getCursor_tag2");
        
        return dbHelper.getIds(_ids, getSortOrder());
    }
    
    public int size() {
        return _ids.size();
    }
    
    public String getSortOrder () {
        return sharedPref.getString("sortOrder", "_id");
    }
    
    public Integer getId(int position) {
        return _ids.get(position);
    }
    
    public ArrayList<String> getColumnArray(String columnName) {
        String METHOD = ".getColumnArray(columnName=" + columnName + ")";
        
        //Log.i(TAG + METHOD, "performance_track getColumnArray_" + columnName + "_start");
        
        ArrayList<String> column = new ArrayList<String>();
        openDbHelper();
        Cursor cursor = dbHelper.getColumn(_ids, columnName, getSortOrder());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            column.add(cursor.getString(0));
            cursor.moveToNext();
        }
//        for (cursor.moveToFirst(); cursor.moveToNext(); cursor.isAfterLast()) {
//            column.add(cursor.getString(0));
//        }
        close();
        
        //Log.i(TAG + METHOD, "performance_track getColumnArray_" + columnName + "_end");
        
        return column;
    }
    
    public HashMap<String, String> getFields(Integer _id) {
        String METHOD = ".getFields(_id=" + _id + "): ";
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        
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
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
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
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        tag = tag.trim();
        logger.log(TAG + METHOD, "trimmed tag query to |" + tag + "|");
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
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        collection = collection.trim();
        logger.log(TAG + METHOD, "trimmed collection query to |" + collection + "|");
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            String rawCollections = currentCursor.getString(currentCursor.getColumnIndex("collections"));
            logger.log(TAG + METHOD, "rawCollections=" + rawCollections);
            String [] collections = rawCollections.split(",");
            boolean collectionMatch = false;
            for (int i = 0; i < collections.length; i++) {
                if (collection.equals(collections[i].trim())) {
                    collectionMatch = true;
                    logger.log(TAG + METHOD, "match found for collections[" + i + "]=" + collections[i].trim());
                    break;
                }
            }
            if (!collectionMatch) {
                logger.log(TAG + METHOD, "about to remove (_ids.size()=" + _ids.size() + ")");
                _ids.remove((Object) currentCursor.getInt(currentCursor.getColumnIndex("_id")));
                logger.log(TAG + METHOD, "removed (_ids.size()=" + _ids.size() + ")");
            }
            currentCursor.moveToNext();
        }
    }
    
    public void restrictByAuthor (String author) {
        String METHOD = "-restrictByAuthor(" + author + "):";
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
        author = author.trim();
        logger.log(TAG + METHOD, "trimmed author query to |" + author + "|");
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            String author1 = currentCursor.getString(currentCursor.getColumnIndex("author1"));
            String author2 = currentCursor.getString(currentCursor.getColumnIndex("author2"));
            String author_other = currentCursor.getString(currentCursor.getColumnIndex("author_other"));
            String authors = author1 + " " + author2 + " " + author_other;
//            logger.log(TAG + METHOD, "authors=" + authors);
            if (!authors.contains(author)) {
                _ids.remove((Object) currentCursor.getInt(currentCursor.getColumnIndex("_id")));
            }
            currentCursor.moveToNext();
        }
    }
    
    public void restrictByReview () {
        String METHOD = "-restrictByReview(): ";
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
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
        logger.log(TAG + METHOD, "start (searchHandler._ids.size()=" + _ids.size() + ")");
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
    
    public ArrayList<String> getCommaSeparatedItemsFromColumn(String columnName) {
        String METHOD = ".getTags()";
        HashSet<String> globalItems = new HashSet<String>();
        openDbHelper();
        Cursor cursor = dbHelper.getColumn(_ids, columnName);
        logger.log(TAG + METHOD, "cursor.getCount()=" + cursor.getCount());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String itemsStr = cursor.getString(0);
//            logger.log(TAG + METHOD, "cursor.position=" + cursor.getPosition() + " tags=" + itemsStr);
            if (itemsStr.contains(",")) {
                String[] items = itemsStr.split(",");
                for (int i = 0; i < items.length; i++) {
                    String item = items[i].trim();
                    globalItems.add(item);
                }
            }
            cursor.moveToNext();
        }
        close();
        return new ArrayList<String>(globalItems); 
    }
    
    public ArrayList<String> getUniqueItemsFromColumn(String columnName) {
        String METHOD = ".getTags()";
        HashSet<String> globalItems = new HashSet<String>();
        openDbHelper();
        Cursor cursor = dbHelper.getColumn(_ids, columnName);
        logger.log(TAG + METHOD, "cursor.getCount()=" + cursor.getCount());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String itemsStr = cursor.getString(0);
            globalItems.add(itemsStr);
            cursor.moveToNext();
        }
        close();
        return new ArrayList<String>(globalItems); 
    }
    
    
    public void close() {
        dbHelper.close();
    }
    
}
