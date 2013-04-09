package com.nietky.librarythingbrowser;

import java.util.ArrayList;

import android.database.Cursor;

public class CursorTags {

    public static ArrayList<String> getTags(Cursor cursor) {
        ArrayList<String> tagsArray = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int tagsIndex = cursor.getColumnIndex("tags");
            String tags = cursor.getString(tagsIndex);
            String[] tagsSA = tags.split(",");
            for (int i = 0; i < tagsSA.length; i++) {
                String tag = tagsSA[i];
                tag = tag.trim();
                if (!tagsArray.contains(tag) && tag != "")
                    tagsArray.add(tag);
            }
            cursor.moveToNext();
        }
        return tagsArray;
    }
    
    public static ArrayList<String> getCollections(Cursor cursor) {
        ArrayList<String> collectionsArray = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int collectionsIndex = cursor.getColumnIndex("collections");
            String collections = cursor.getString(collectionsIndex);
            String[] collectionsSA = collections.split(",");
            for (int i = 0; i < collectionsSA.length; i++) {
                String collection = collectionsSA[i];
                collection = collection.trim();
                if (!collectionsArray.contains(collection) && collection != "")
                    collectionsArray.add(collection);
            }
            cursor.moveToNext();
        }
        return collectionsArray;
    }
    
    public static ArrayList<String> getAuthors1(Cursor cursor) {
        ArrayList<String> authorsArray = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int authorsIndex = cursor.getColumnIndex("author1");
            String author = cursor.getString(authorsIndex);
            if (!authorsArray.contains(author) && author != "")
                authorsArray.add(author);
            cursor.moveToNext();
        }
        return authorsArray;
    }
    
    
    
}
