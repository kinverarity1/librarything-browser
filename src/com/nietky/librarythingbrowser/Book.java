package com.nietky.librarythingbrowser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

import android.content.ContentValues;

public class Book {

    public int dbID;
    public int bookID;                 // 24156389
    public String title;               // Terra Australis : Matthew Flinders' great adventures in the circumnavigation of Australia
    public String authorLastFirst;     // Flinders, Matthew
    public String authorFirstLast;     // Matthew Flinders
    public String authorOthers;        // R. B. Wilson, A. W. G. Whittle
    public String publication;         // Simon & Schuster (1989), Unknown Binding, 224 pages
    public String date;                // 1939
    public String ISBN;                // [], [0316085251], [014044534X]
    public String series;              // Seal books
    public String source;              // National Library of Australia
    public String language1;           // English, (blank)
    public String language2;           // English, (blank)
    public String language3;           // English, (blank)
    public String LCC;                 // HD2177.M4 1970
    public String DDC;                 // 333.7/6/099423
    public String bookCrossingID;      // ...?
    public GregorianCalendar dateEntered;
    public GregorianCalendar dateAcquired;
    public GregorianCalendar dateStarted;
    public GregorianCalendar dateFinished;
    public float stars;                  // 1, 1.5, etc. 5
    public String[] collections;
    public String[] tags;
    public String review;
    public String comments;
    public String commentsPrivate;
    public int copies;
    public String encoding;
    
    public final static int DATE_ENTERED = 0;
    public final static int DATE_ACQUIRED = 1;
    public final static int DATE_STARTED = 2;
    public final static int DATE_FINISHED = 3;
    
    public String getDateEnteredAsString () {
        return getDateAsString(DATE_ENTERED);
    }
    
    public String getDateAcquiredAsString () {
        return getDateAsString(DATE_ENTERED);
    }
    
    public String getDateStartedAsString () {
        return getDateAsString(DATE_ENTERED);
    }
    
    public String getDateFinishedAsString () {
        return getDateAsString(DATE_ENTERED);
    }
    
    public String getCollectionsAsString () {
        String s = "";
        for (int i = 0; i < collections.length; i++) {
            s += collections[i];
            if (i < collections.length - 1)
                s += ",";
        }
        return s;
    }
    
    public String getTagsAsString () {
        String s = "";
        for (int i = 0; i < tags.length; i++) {
            s += tags[i];
            if (i < tags.length - 1)
                s += ",";
        }
        return s;
    }
    
    public String getField (String fieldName) {
        String value = "";
        if (fieldName.contains("dbID"))
            value = String.valueOf(dbID);
        else if (fieldName.contains("bookID"))
            value = String.valueOf(bookID);
        else if (fieldName.contains("title"))
            value = title;
        else if (fieldName.contains("authorLastFirst"))
            value = authorLastFirst;
        else if (fieldName.contains("authorFirstLast"))
            value = authorFirstLast;
        else if (fieldName.contains("authorOthers"))
            value = authorOthers;
        else if (fieldName.contains("publication"))
            value = publication;
        else if (fieldName.contains("date"))
            value = date;
        else if (fieldName.contains("ISBN"))
            value = ISBN;
        else if (fieldName.contains("series"))
            value = series;
        else if (fieldName.contains("source"))
            value = source;
        else if (fieldName.contains("language1"))
            value = language1;
        else if (fieldName.contains("language2"))
            value = language2;
        else if (fieldName.contains("language3"))
            value = language3;
        else if (fieldName.contains("LCC"))
            value = LCC;
        else if (fieldName.contains("DDC"))
            value = DDC;
        else if (fieldName.contains("bookCrossingID"))
            value = bookCrossingID;
        else if (fieldName.contains("dateEntered"))
            value = getDateEnteredAsString();
        else if (fieldName.contains("dateAcquired"))
            value = getDateAcquiredAsString();
        else if (fieldName.contains("dateStarted"))
            value = getDateStartedAsString();
        else if (fieldName.contains("dateFinished"))
            value = getDateFinishedAsString();
        else if (fieldName.contains("stars"))
            value = String.valueOf(stars);
        else if (fieldName.contains("collections"))
            value = getCollectionsAsString();
        else if (fieldName.contains("tags"))
            value = getTagsAsString();
        else if (fieldName.contains("review"))
            value = review;
        else if (fieldName.contains("commentsPrivate"))
            value = commentsPrivate;
        else if (fieldName.contains("comments"))
            value = comments;
        else if (fieldName.contains("copies"))
            value = String.valueOf(copies);
        else if (fieldName.contains("encoding"))
            value = encoding;
        return value;
    }
    
    public String getDateAsString (int dateType) {
        GregorianCalendar date = new GregorianCalendar();
        switch (dateType) {
            case (DATE_ENTERED):
                date = this.dateEntered;
                break;
            case (DATE_ACQUIRED):
                date = this.dateAcquired;
                break;
            case (DATE_STARTED):
                date = this.dateStarted;
                break;
            case (DATE_FINISHED):
                date = this.dateFinished;
                break;
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return ((String) df.format(date));
    }
    
    public ContentValues getContentValues () {
        ContentValues values = new ContentValues();
        values.put("_id", dbID);
        values.put("bookID", bookID);
        values.put("title", title);
        values.put("authorLastFirst", authorLastFirst);
        values.put("authorFirstLast", authorFirstLast);
        values.put("authorOthers", authorOthers);
        values.put("publication", publication);
        values.put("date", date);
        values.put("ISBN", ISBN);
        values.put("series", series);
        values.put("source", source);
        values.put("language1", language1);
        values.put("language2", language2);
        values.put("language3", language3);
        values.put("LCC", LCC);
        values.put("DDC", DDC);
        values.put("bookCrossingID", bookCrossingID);
        values.put("dateEntered", getDateEnteredAsString());
        values.put("dateAcquired", getDateAcquiredAsString());
        values.put("dateStarted", getDateStartedAsString());
        values.put("dateFinished", getDateFinishedAsString());
        values.put("stars", String.valueOf(stars));
        values.put("collections", getCollectionsAsString());
        values.put("tags", getTagsAsString());
        values.put("review", review);
        values.put("comments", comments);
        values.put("commentsPrivate", commentsPrivate);
        values.put("copies", String.valueOf(copies));
        values.put("encoding", encoding);
        return values;
    }
    
    public static GregorianCalendar parseDate (String s) {
        // For things like:
        //
        // Jan 4, 2013
        // Apr 11, 1934
        //
        s = s.trim();
        if (s.substring(5, 6).contains(","))
            s = s.substring(0, 4) + "0" + s.substring(4);
        String monthString = s.substring(0, 3);
        int month = -1;
        if (monthString.contains("Jan"))
            month = GregorianCalendar.JANUARY;
        else if (monthString.contains("Feb"))
            month = GregorianCalendar.FEBRUARY;
        else if (monthString.contains("Mar"))
            month = GregorianCalendar.MARCH;
        else if (monthString.contains("Apr"))
            month = GregorianCalendar.APRIL;
        else if (monthString.contains("May"))
            month = GregorianCalendar.MAY;
        else if (monthString.contains("Jun"))
            month = GregorianCalendar.JUNE;
        else if (monthString.contains("Jul"))
            month = GregorianCalendar.JULY;
        else if (monthString.contains("Aug"))
            month = GregorianCalendar.AUGUST;
        else if (monthString.contains("Sep"))
            month = GregorianCalendar.SEPTEMBER;
        else if (monthString.contains("Oct"))
            month = GregorianCalendar.OCTOBER;
        else if (monthString.contains("Nov"))
            month = GregorianCalendar.NOVEMBER;
        else if (monthString.contains("Dec"))
            month = GregorianCalendar.DECEMBER;
        String day = s.substring(4, 6);
        String year = s.substring(8);
        GregorianCalendar d = new GregorianCalendar();
        d.set(Integer.valueOf(year), month, Integer.valueOf(day));
        return d;
    }
    
}
