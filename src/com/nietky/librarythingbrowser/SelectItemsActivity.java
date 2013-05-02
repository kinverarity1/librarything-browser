package com.nietky.librarythingbrowser;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectItemsActivity extends Activity {
    public static final String TAG = "SelectItemsActivity";

    public static final int TYPE_FIELDS = 0;
    public static final int TYPE_COLLECTIONS = 1;
    public static final int TYPE_TAGS = 2;

    private int type;
    private SearchHandler searchHandler;
    private String ids = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_items);
        
        Intent intent = getIntent();
        this.type = intent.getIntExtra("type", this.TYPE_FIELDS);
        this.ids = intent.getStringExtra("ids");
        
        ListView listView = (ListView) findViewById(R.id.dialog_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_multiple_choice);
        ArrayList<String> array = getArrayOfItems();
        for (int i = 0; i < array.size(); i++) {
            adapter.add(array.get(i));
        }
        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);
        setTitle(getSpecialTitle());
    }

    public String getSpecialTitle() {
        String string = "";
        switch (type) {
        case TYPE_FIELDS:
            string = "Select fields to search";
            break;
        case TYPE_COLLECTIONS:
            string = "Restrict to collections";
            break;
        case TYPE_TAGS:
            string = "Restrict to tags";
            break;
        }
        return string;
    }

    public ArrayList<String> getArrayOfItems() {
        ArrayList<String> array = new ArrayList<String>();
        if (type == TYPE_FIELDS) {
            array.add("Comments");
            array.add("Publication details");
            array.add("Title");
        } else {
            CursorTags cursorTags = new CursorTags();
            SearchHandler searchHandler = new SearchHandler(this);
            searchHandler.setIds(ids);
            Cursor cursor = searchHandler.getCursor();
            switch (type) {
            case TYPE_COLLECTIONS:
                array = cursorTags.getCollections(cursor);
                Collections.sort(array, new CursorTags.LowercaseAlphaComparator());
                break;
            case TYPE_TAGS:
                array = cursorTags.getTags(cursor);
                Collections.sort(array, new CursorTags.LowercaseAlphaComparator());
                break;
            }
            searchHandler.close();
        }
        return array;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select_items, menu);
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
