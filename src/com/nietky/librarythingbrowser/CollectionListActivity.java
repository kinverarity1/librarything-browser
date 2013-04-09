package com.nietky.librarythingbrowser;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CollectionListActivity extends Activity {
    ArrayList<String> collections;
    ListView listView;
    
    SearchHandler searchHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_list);
        
        listView = (ListView) findViewById(R.id.collectionListView);

        Intent intent = getIntent();
        searchHandler = new SearchHandler(this);
        searchHandler.setIds(intent.getStringExtra("ids"));
        collections = CursorTags.getCollections(searchHandler.getCursor());
        Collections.sort(collections, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, collections);
        
        listView.setAdapter(adapter);
        setTitle(getString(R.string.title_activity_collection_list) + " (" + collections.size() + "):");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                String collection = collections.get(position);
                Intent resultIntent = new Intent(parent.getContext(), BookListActivity.class);
                resultIntent.putExtra("collectionName", collection);
                startActivity(resultIntent);
            }
        });
    }
    
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // String selection = l.getItemAtPosition(position).toString();
        String collection = collections.get(position);
        Intent intent = new Intent(this, BookListActivity.class);
        intent.putExtra("collectionName", collection);

        // The following MUST be made subject to a preference for exclusive/inclusive/ask tag/collection handling
        intent.putExtra("ids", getIds());
        
        startActivity(intent);
    }
    
    protected String getIds() {
        return searchHandler.getString();
    }

}
