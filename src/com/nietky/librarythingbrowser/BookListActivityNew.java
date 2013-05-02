package com.nietky.librarythingbrowser;

import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class BookListActivityNew extends ListActivity {
    String TAG = "BookListActivityNew";
    LogHandler logger;
    Intent intent;
    InputStreamReader inputStreamReader = null;
    
    int RESULT_TAG_SELECT = 1;
    int RESULT_COLLECTION_SELECT = 2;
    
    
    
    public final static int VIEW_TYPE_ADVANCED_SEARCH = 0;
    public final static int VIEW_TYPE_HEADER = 1;
    public final static int VIEW_TYPE_BOOK = 2;
    
    Query query;

    BookListAdapterNew adapter;
    
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefsEdit;

    public void onCreate(Bundle savedInstanceState) {
        String METHOD = ".onCreate()";
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        logger = new LogHandler(sharedPref);
        logger.log(TAG + METHOD, "Start");
        
        Query queryAllBooks = new Query(this);
        queryAllBooks.setToAllBooks();
        if (queryAllBooks.getSize() == 0)
            startActivity(new Intent(this, LoginActivity.class));
        
        query = new Query(this);
        setTitle("All books");
        filterByLaunchIntent();
    }
        
    private void filterByLaunchIntent () {
        intent = getIntent();
        String action = intent.getAction();
        if (intent.hasExtra("dbIDs")) {
            String dbIDString = intent.getStringExtra("dbIDs");
            query.setBooksFromDbIDs(dbIDString);
        }
        if (Intent.ACTION_SEARCH.equals(action)) {
            String searchQuery = "";
            if (intent.hasExtra("queryGeneral"))
                searchQuery = intent.getStringExtra("queryGeneral");
            else
                searchQuery = intent.getStringExtra(SearchManager.QUERY);
            query.restrictByQueryInAll(searchQuery);
        } else if (intent.hasExtra("selectTag")) {
            String value = intent.getStringExtra("selectTag");
            query.restrictByTag(value);
        } else if (intent.hasExtra("selectCollection")) {
            String value = intent.getStringExtra("selectCollection");
            query.restrictByCollection(value);
        } else if (intent.hasExtra("selectAuthor")) {
            String value = intent.getStringExtra("selectAuthor");
            query.restrictByAuthor(value);
        }
    }
    
    public void onResume () {
        super.onResume();
        refreshBookList();
    }
    
    private void refreshBookList () {
        adapter = new BookListAdapterNew(this, query.getBooks(), true);
        getListView().setFastScrollEnabled(true);
        setListAdapter(adapter);
    }
    
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        int viewType = adapter.getItemViewType(position);
        switch (viewType) {
            case (VIEW_TYPE_ADVANCED_SEARCH):
                startActivity(new Intent(this, AdvancedSearchActivity.class));
                break;
            case (VIEW_TYPE_BOOK):
                position = adapter.getBookListPosition(position);
                Book book = query.getBooks().get(position);
                Intent intent = new Intent(this, BookDetailActivity.class);
                intent.putExtra("dbID", book.dbID);
                intent.putExtra("dbIDs", query.getDbIDsAsString());
                startActivity(intent);
                break;
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuPreferences:
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        case R.id.menuSearch:
            this.onSearchRequested();
            return true;
        case R.id.menuDelete:
            final DbHelper dbHelper = new DbHelper(getApplicationContext());
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Delete books")
                    .setMessage(
                            "Are you sure you want to delete all your books?\n\n(This is only for data imported into LibraryThing Browser on your device, and cannot affect your LibraryThing account).")
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dbHelper.deleteTable();
                                    refreshBookList();
                                }
                            }).setNegativeButton("No", null).show();
            return true;
        case R.id.menuShowAllBooks:
            startActivity(new Intent(this, BookListActivity.class));
            return true;
        case R.id.menuTags:
            startActivityWithIds(new Intent(this, TagListActivity.class));
            return true;
        case R.id.menuCollections:
            startActivityWithIds(new Intent(this, CollectionListActivity.class));
            return true;
        case R.id.menuAuthors:
            startActivityWithIds(new Intent(this, AuthorListActivity.class));
            return true;
        case R.id.menuImport:
            ImportTask importTask = new ImportTask(this);
            importTask.execute();
            return true;
        case R.id.menuReviews:
            startActivityWithIds(new Intent(this, ReviewListActivity.class));
            return true;
        case R.id.menuComments:
            startActivityWithIds(new Intent(this, CommentListActivity.class));
            return true;
        case R.id.menu_advanced_search:
            Intent intent = new Intent(this, AdvancedSearchActivity.class);
            intent.putExtra("dbIDs", query.getDbIDsAsString());
            startActivity(intent);
            return true;
        default:
            return false;
        }
    }
    
    public void startActivityWithIds (Intent intent) {
        intent.putExtra("dbIDs", query.getDbIDsAsString());
        startActivity(intent);
    }
    
}
