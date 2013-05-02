package com.nietky.librarythingbrowser;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Query {
    private String TAG = "Query"; 

    private SharedPreferences sharedPref;
    private Context context;
    
    private List<Book> books;
    private DbHelper dbHelper;
    
    public Query (Context context) {
        String METHOD = ".Query constructor(context)";
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        books = new ArrayList<Book>();
        dbHelper = new DbHelper(context.getApplicationContext());
    }
    
    public void setBooks (List<Book> books) {
        this.books = books;
    }
    
    public void setToAllBooks () {
        this.books = dbHelper.getAllBooks();
    }
    
    public void setBooksFromDbIDs (List<Integer> dbIDs) {
        books = dbHelper.getBooksFromDbIDs(dbIDs);
    }
    
    public void setBooksFromDbIDs (String dbIDs) {
        String[] IDs = dbIDs.split(",");
        List<Integer> dbIDList = new ArrayList<Integer>();
        for (int i = 0; i < IDs.length; i++)
            dbIDList.add(Integer.valueOf(IDs[i]));
        setBooksFromDbIDs(dbIDList);
    }
    
    public List<Book> getBooks () {
        return books;
    }
    
    public List<String> getField (String fieldName) {
        List<String> values = new ArrayList<String>();
        for (Book book : books)
            values.add(book.getField(fieldName));
        return values;
    }
    
    public List<Integer> getDbIDs () {
        List<Integer> values = new ArrayList<Integer>();
        for (Book book : books)
            values.add(book.dbID);
        return values;
    }
    
    public String getDbIDsAsString () {
        String IDs = "";
        for (int i = 0; i < books.size(); i++) {
            IDs += String.valueOf(books.get(i).dbID);
            if (i < books.size() - 1)
                IDs += ",";
        }
        return IDs;
    }
    
    public int getSize () {
        return books.size();
    }
    
    private String adjustCaseByPreference (String before) {
        String after = before;
        if (!sharedPref.getBoolean("prefCaseSensitiveSearch", false))
            after = before.toLowerCase();
        return after;
    }
    
    public void restrictByQueryInAll (String query) {
        restrictByQueryInFields(query.trim(), dbHelper.KEYS);
    }
    
    public void restrictByQueryInFields (String query, String[] fieldNames) {
        query = adjustCaseByPreference(query.trim());
        for (Book book : books) {
            boolean keep = false;
            for (int j = 0; j < fieldNames.length; j++) {
                String fieldValue = book.getField(fieldNames[j]).trim();
                fieldValue = adjustCaseByPreference(fieldValue);
                if (fieldValue.contains(query)) {
                    keep = true;
                    break;
                }
            }
            if (!keep)
                books.remove(book);
        }
    }
    
    public void restrictByTag (String query) {
        query = adjustCaseByPreference(query.trim());
        for (Book book : books) {
            boolean keep = false;
            for (int i = 0; i < book.tags.length; i++) {
                String fieldValue = adjustCaseByPreference(book.tags[i]).trim();
                if (fieldValue.contains(query) && query.contains(fieldValue)) {
                    keep = true;
                    break;
                }
            }
            if (!keep)
                books.remove(book);
        }
    }
    
    public void restrictByCollection (String query) {
        query = adjustCaseByPreference(query.trim());
        for (Book book : books) {
            boolean keep = false;
            for (int i = 0; i < book.collections.length; i++) {
                String fieldValue = adjustCaseByPreference(book.collections[i]).trim();
                if (fieldValue.contains(query) && query.contains(fieldValue)) {
                    keep = true;
                    break;
                }
            }
            if (!keep)
                books.remove(book);
        }
    }
    
    public void restrictByAuthor (String query) {
        query = query.toLowerCase().trim();
        for (Book book : books) {
            String author = book.authorFirstLast +
                    " " + book.authorLastFirst +
                    " " + book.authorOthers;
            if (!author.contains(query))
                books.remove(book);
        }
    }
    
    public void restrictByReview () {
        for (Book book : books) {
            if (book.review.trim().length() == 0)
                books.remove(book);
        }
    }
    
    public void restrictByComments () {
        for (Book book : books) {
            if (book.comments.trim().length() == 0 && book.commentsPrivate.trim().length() == 0)
                books.remove(book);
        }
    }
}
















