package com.nietky.librarythingbrowser;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class AuthorListActivity extends Activity {
    String TAG = "AuthorListActivity";
    SharedPreferences sharedPref;
    LogHandler logger;
    
    ArrayList<String> authors;
    ListView listView;
    ArrayList<String> alpha;
    
    SearchHandler searchHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_list);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        logger = new LogHandler(sharedPref);
        
        listView = (ListView) findViewById(R.id.authorListView);

        Intent intent = getIntent();
        searchHandler = new SearchHandler(this);
        searchHandler.setIds(intent.getStringExtra("ids"));
        authors = CursorTags.getAuthors1(searchHandler.getCursor());
        Collections.sort(authors, String.CASE_INSENSITIVE_ORDER);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, authors);
        SectionIndexingArrayAdapter<String> adapter = new SectionIndexingArrayAdapter(this, android.R.layout.simple_list_item_1, authors);
        listView.setAdapter(adapter);
        setTitle(getString(R.string.title_activity_author_list) + " (" + authors.size() + "):");
        listView.setFastScrollEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                String author = authors.get(position);
                Intent intent = new Intent(parent.getContext(), BookListActivity.class);
                intent.putExtra("authorName", author);
                // The following MUST be made subject to a preference for exclusive/inclusive/ask tag/collection handling
                intent.putExtra("ids", getIds());
                startActivity(intent);
            }
        });
    }

    protected String getIds() {
        return searchHandler.getString();
    }
}
