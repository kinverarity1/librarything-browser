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

public class CollectionListActivity extends Activity {
    String TAG = "CollectionListActivity";
    LogHandler logger;
    SharedPreferences sharedPref;
    ArrayList<String> collections;
    ListView listView;
    SearchHandler searchHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String METHOD = ".onCreate()";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_list);
        
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        logger = new LogHandler(sharedPref);
        logger.log(TAG + METHOD, "Start");
        
        listView = (ListView) findViewById(R.id.collectionListView);

        Intent intent = getIntent();
        searchHandler = new SearchHandler(this);
        searchHandler.setIds(intent.getStringExtra("ids"));
        collections = searchHandler.getCommaSeparatedItemsFromColumn("collections");
        Collections.sort(collections, String.CASE_INSENSITIVE_ORDER);
        String collectionsString = "";
        for (String collection : collections) {
            collectionsString += ',' + collection;
        }
        logger.log(TAG + METHOD, "collections=" + collectionsString);
        SectionIndexingArrayAdapter<String> adapter = new SectionIndexingArrayAdapter<String>(this, android.R.layout.simple_list_item_1, collections);
        
        listView.setAdapter(adapter);
        setTitle(getString(R.string.title_activity_collection_list) + " (" + collections.size() + "):");
        listView.setFastScrollEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                String collection = collections.get(position);
                Intent intent = new Intent(parent.getContext(), BookListActivity.class);
                intent.putExtra("collectionName", collection.trim());
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
