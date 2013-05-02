package com.nietky.librarythingbrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    
    public static class BookProperty {
        public Integer id;
        public String property;
        public ArrayList<String> otherData;
        
        public BookProperty (Integer id, String property, ArrayList<String> otherData) {
            this.id = id;
            this.property = property;
            this.otherData = otherData;
        }
    }
    
    public void sortByDate (String columnName) {
        String METHOD = ".sortByDate(columnName = " + columnName + ")";
        ArrayList<String> dates = getColumnArray(columnName);
        ArrayList<BookProperty> books = new ArrayList<BookProperty>();
        ArrayList<Integer> ids = getIds();
        for (int i = 0; i < dates.size(); i++) {
            books.add(new BookProperty(ids.get(i), dates.get(i), null));
        }
        logger.log(TAG + METHOD, "ids[0]=" + ids.get(0) + ", [-1]=" + ids.get(ids.size() - 1));
        Collections.sort(books, new DateComparator(false));
        ArrayList<Integer> newIds = new ArrayList<Integer>();
        for (int i = 0; i < books.size(); i++)
            newIds.add(books.get(i).id);
        logger.log(TAG + METHOD, "newIds[0]=" + newIds.get(0) + ", [-1]=" + newIds.get(newIds.size() - 1));
        setIds(newIds);
    }
    
    public static class DateComparator implements Comparator<BookProperty> {
        static HashMap<String,String> dateMap = new HashMap<String,String>();        
        boolean reverse;
        
        public DateComparator (boolean reverse) {
            dateMap.put("Jan", "01");
            dateMap.put("Feb", "02");
            dateMap.put("Mar", "03");
            dateMap.put("Apr", "04");
            dateMap.put("May", "05");
            dateMap.put("Jun", "06");
            dateMap.put("Jul", "07");
            dateMap.put("Aug", "08");
            dateMap.put("Sep", "09");
            dateMap.put("Oct", "10");
            dateMap.put("Nov", "11");
            dateMap.put("Dec", "12");
            this.reverse = reverse;
        }
        
        public int compare(BookProperty bp1, BookProperty bp2) {
            String d1 = bp1.property;
            String d2 = bp2.property;
            String dn1 = "";
            String dn2 = "";
//            "May 6, 2010"     -- comma at 5
//            "May 27, 2010"    -- comma at 6
            if (d1.trim().length() == 0)
                if (d2.trim().length() == 0)
                    return 0;
                else {
                    return 1;
                }
            if (d2.trim().length() == 0)
                return -1;
            
            if (d1.substring(5, 6).contains(","))
                d1 = d1.substring(0, 4) + "0" + d1.substring(4);
            if (d2.substring(5, 6).contains(","))
                d2 = d2.substring(0, 4) + "0" + d2.substring(4);
            dn1 = d1.substring(8) + dateMap.get(d1.substring(0, 3)) + d1.substring(4, 6);
            dn2 = d2.substring(8) + dateMap.get(d2.substring(0, 3)) + d2.substring(4, 6);
            if (reverse) {
                String temp = dn2;
                dn2 = dn1;
                dn1 = temp;
            }
            return dn1.toLowerCase().compareTo(dn2.toLowerCase());
        }
    }
    
    public static class StringComparator implements Comparator<BookProperty> {       
        boolean reverse;
        public StringComparator (boolean reverse) {
            this.reverse = reverse;
        }
        public int compare(BookProperty bp1, BookProperty bp2) {
            String d1 = bp1.property;
            String d2 = bp2.property;
            if (reverse) {
                String temp = d2;
                d2 = d1;
                d1 = temp;
            }
            return d1.toLowerCase().compareTo(d2.toLowerCase());
        }
    }
    
    public void close() {
        dbHelper.close();
    }
    
}
