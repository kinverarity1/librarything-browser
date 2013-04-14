package com.nietky.librarythingbrowser;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CommentListActivity extends Activity {
    String TAG = "CommentListActivity";
    SharedPreferences sharedPref;
    LogHandler logger;
    
    CommentListCursorAdapter adapter;
    ListView listView;
    Cursor cursor;
    SearchHandler searchHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String METHOD = ":onCreate(): ";
        Log.d(TAG + METHOD, "start");
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewlist);
        
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        logger = new LogHandler(sharedPref);
        
        listView = (ListView) findViewById(R.id.reviewListView);
        listView.setFastScrollEnabled(true);
        Intent intent = getIntent();
        searchHandler = new SearchHandler(this);
        searchHandler.setIds(intent.getStringExtra("ids"));
        searchHandler.restrictByComments();
        cursor = searchHandler.getCursor();
        
        this.setTitle(getString(R.string.comments_ui_title) + " (" + cursor.getCount() + ")");
        adapter = new CommentListCursorAdapter(this, cursor);
        listView.setAdapter(adapter);
        
    }
    
    public class CommentListCursorAdapter extends CursorAdapter {
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
        public CommentListCursorAdapter(Context context, Cursor c) {
            super(context, c);
            _context = context; 
            inflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView titleText = (TextView) view
                    .findViewById(R.id.commentlist_item_title);
            TextView authorText = (TextView) view
                    .findViewById(R.id.commentlist_item_author);
            TextView commentsText = (TextView) view
                    .findViewById(R.id.commentlist_item_comments);
            TextView commentsPrivateText = (TextView) view
                    .findViewById(R.id.commentlist_item_comments_private);
            
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
            
            String comments = cursor.getString(cursor.getColumnIndex("comments"));
            String comments_private = cursor.getString(cursor.getColumnIndex("comments_private"));
            if (comments.trim().length() > 0) {
                commentsText.setText(FormatText.asHtml(comments));
            } else {
                commentsText.setVisibility(commentsText.INVISIBLE);
            }
            if (comments_private.trim().length() > 0) {
                commentsPrivateText.setText(FormatText.asHtml(comments_private));
            } else {
                commentsPrivateText.setVisibility(commentsPrivateText.INVISIBLE);
            }
            
            
            // if
            // (cursor.getString(cursor.getColumnIndex("tags")).contains("unread"))
            // {
            // view.setBackgroundColor(Color.GRAY);
            // } else
            // view.setBackgroundColor(Color.BLACK);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(R.layout.commentlist_item, parent, false);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comment_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
