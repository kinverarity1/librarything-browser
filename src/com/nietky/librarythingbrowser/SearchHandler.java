package com.nietky.librarythingbrowser;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class SearchHandler {
    private static final String TAG = "SearchHandler";
    
    private Context _context;
    private ArrayList<Integer> _ids;

    public DbHelperNew dbHelper;

    public SearchHandler(Context context) {
        String METHOD = ".constructor(Context): ";
        Log.d(TAG + METHOD, "start");
        
        _context = context;
        dbHelper = new DbHelperNew(_context.getApplicationContext());
        dbHelper.open();
        
        _ids = new ArrayList<Integer> ();
    }
    
    public void setIds () {
        _ids = dbHelper.getAllIds();
    }
    
    public void setIds (String commaSeparatedIds) {
        String[] ids = commaSeparatedIds.split(",");
        for (int i = 0; i < ids.length; i++) {
            int id = Integer.valueOf(ids[i]);
            _ids.add(id);
        }
    }
    
    public void setIds(ArrayList<Integer> ids) {
        _ids = ids;
    }
    
    public ArrayList<Integer> getIds() {
        return _ids;
    }
    
    public String getString () {
        String s = "";
        for (int i = 0; i < _ids.size(); i++) {
            s += String.valueOf(_ids.get(i)) + ",";
        }
        return s;
    }
    
    public Cursor getCursor() {
        String METHOD = ".getCurrentCursor(): ";
        Log.d(TAG + METHOD, "start");
        
        return dbHelper.getIds(_ids);
    }

    public void restrictByQuery(String query) {
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            int nCols = currentCursor.getColumnCount();
            int idColN = currentCursor.getColumnIndex("_id");
            boolean queryMatch = false;
            for (int iCol = 0; iCol < nCols; iCol++) {
                if (iCol == idColN) continue;
                String fieldValue = currentCursor.getString(iCol);
                if (fieldValue.contains(query)) {
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
                    Log.d(TAG + METHOD, "match found for collections[" + i + "]=" + collections[i]);
                    break;
                }
            }
            if (!collectionMatch) {
                _ids.remove((Object) currentCursor.getInt(currentCursor.getColumnIndex("_id")));
            }
            currentCursor.moveToNext();
        }
    }
    
    public void restrictByReview () {
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            String review = currentCursor.getString(currentCursor.getColumnIndex("review"));
            if (review.trim().isEmpty()) {
                _ids.remove((Object) currentCursor.getInt(currentCursor.getColumnIndex("_id")));
            }
            currentCursor.moveToNext();
        }
    }
    public void restrictByComments () {
        Cursor currentCursor = getCursor();
        currentCursor.moveToFirst();
        while (!currentCursor.isAfterLast()) {
            String comments = currentCursor.getString(currentCursor.getColumnIndex("comments"));
            String comments_private = currentCursor.getString(currentCursor.getColumnIndex("comments_private"));
            if (comments.trim().isEmpty() && comments_private.trim().isEmpty()) {
                _ids.remove((Object) currentCursor.getInt(currentCursor.getColumnIndex("_id")));
            }
            currentCursor.moveToNext();
        }
    }
    
    
    public void close() {
        dbHelper.close();
    }
    
}
