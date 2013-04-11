package com.nietky.librarythingbrowser;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ReviewListActivity extends Activity {
    public final static String TAG = "ReviewListActivity";
    
    ReviewListCursorAdapter adapter;
    ListView listView;
    Cursor cursor;
    SearchHandler searchHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String METHOD = ":onCreate(): ";
        Log.d(TAG + METHOD, "start");
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewlist);
        
        listView = (ListView) findViewById(R.id.reviewListView);
        listView.setFastScrollEnabled(true);
        Intent intent = getIntent();
        searchHandler = new SearchHandler(this);
        searchHandler.setIds(intent.getStringExtra("ids"));
        searchHandler.restrictByReview();
        cursor = searchHandler.getCursor();
        
        this.setTitle(getString(R.string.reviews_ui_title) + " (" + cursor.getCount() + ")");
        adapter = new ReviewListCursorAdapter(this, cursor);
        listView.setAdapter(adapter);
    }
    
    public class ReviewListCursorAdapter extends CursorAdapter {
        LayoutInflater inflater;
        Context _context;
        String[] fieldnames = { "_id", "book_id", "title", "author1",
                "author2", "author_other", "publication", "date", "ISBNs",
                "series", "source", "lang1", "lang2", "lang_orig", "LCC",
                "DDC", "bookcrossing", "date_entered", "date_acquired",
                "date_started", "date_ended", "stars", "collections", "tags",
                "review", "summary", "comments", "comments_private", "copies",
                "encoding" };
        
        @SuppressWarnings("deprecation")
        public ReviewListCursorAdapter(Context context, Cursor c) {
            super(context, c);
            _context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView titleText = (TextView) view
                    .findViewById(R.id.reviewlist_item_title);
            TextView authorText = (TextView) view
                    .findViewById(R.id.reviewlist_item_author);
            TextView reviewText = (TextView) view
                    .findViewById(R.id.reviewlist_item_review);
            
            HashMap fields = new HashMap<String, String>();

            String fieldname;
            
            for (int i = 0; i < fieldnames.length; i += 1) {
                fieldname = fieldnames[i];
//                Log.d(TAG, "Getting content for fieldname=" + fieldname);
                int index = cursor.getColumnIndex(fieldname);
                String content = "";
                if (index > -1)
                    content = cursor.getString(index);
                content = content.replace("[return]", "\n");
                content = content.trim();
                fields.put(fieldname, content);
            }
            
            titleText.setText(FormatText.asHtml(cursor.getString(cursor.getColumnIndex("title"))));
            final String _id = cursor.getString(cursor.getColumnIndex("_id"));
            titleText.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(_context, BookDetailActivity.class);
                    intent.putExtra("_id", _id);
                    startActivity(intent);
                }
            });
            
            authorText.setText((cursor.getString(cursor
                      .getColumnIndex("author2"))));
            reviewText.setText(FormatText.asHtml(cursor.getString(cursor.getColumnIndex("review"))));
            // if
            // (cursor.getString(cursor.getColumnIndex("tags")).contains("unread"))
            // {
            // view.setBackgroundColor(Color.GRAY);
            // } else
            // view.setBackgroundColor(Color.BLACK);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(R.layout.reviewlist_item, parent, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.review_list, menu);
        return true;
    }
}
