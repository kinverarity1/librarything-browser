package com.nietky.librarythingbrowser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;

public class BookListActivity extends ListActivity {
    String TAG = "BookListActivity";
    LogHandler logger;
    Intent intent;
    String MESSAGE_TABLE_NAME = "com.nietky.librarythingbrowser.TABLE_NAME";
    InputStreamReader inputStreamReader = null;
    
    int RESULT_TAG_SELECT = 1;
    int RESULT_COLLECTION_SELECT = 2;
    
    int PROGRESS_LOGGED_IN = 3;
    int PROGRESS_SUCCESS = 4;
    int PROGRESS_LOGIN_FAIL = 5;

    Cursor cursor;
    ArrayList<Integer> _ids = new ArrayList<Integer>();
    SearchHandler searchHandler;
    ArrayList<ClickThrough> queryClickThroughs = new ArrayList<ClickThrough>();
    BookListAdapter adapter;
    
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefsEdit;
    
    HttpContext localContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        String METHOD = ".onCreate()";
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        logger = new LogHandler(sharedPref);
        logger.log(TAG + METHOD, "Start");
        
        CookieStore cookieStore = new BasicCookieStore();
        localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        searchHandler = new SearchHandler(this);
        searchHandler.setIds();
        setTitle(getString(R.string.topbar_allbooks));
        
        intent = getIntent();
        String action = intent.getAction();
        if (intent.hasExtra("ids")) {
            logger.log(TAG + METHOD, "Intent.hasExtra('ids')");
            searchHandler.setIds(intent.getStringExtra("ids"));
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            logger.log(TAG + METHOD, "Intent.ACTION_VIEW");
            importData();
        } else if (Intent.ACTION_SEARCH.equals(action)) {
            String query = "";
            if (intent.hasExtra("generalQuery"))
                query = intent.getStringExtra("generalQuery");
            else
                query = intent.getStringExtra(SearchManager.QUERY);
            logger.log(TAG + METHOD, "Intent.ACTION_SEARCH query=" + query);
            String previousIds = searchHandler.getString();
            searchHandler.restrictByQuery(query);
            setTitle(query);
            queryClickThroughs.add(new ClickThrough(this, query, ClickThrough.QUERY_ADVANCED, previousIds, searchHandler.getString()));
            loadList();
        } else if (intent.hasExtra("tagName")) {
            String tag = intent.getStringExtra("tagName");
            logger.log(TAG + METHOD, "Intent has an extra: tagName=" + tag);
            String previousIds = searchHandler.getString();
            searchHandler.restrictByTag(tag); 
            setTitle(tag);
            queryClickThroughs.add(new ClickThrough(this, tag, ClickThrough.QUERY_ADVANCED, previousIds, searchHandler.getString()));
            loadList();
        } else if (intent.hasExtra("collectionName")) {
            String collection = intent.getStringExtra("collectionName");
            logger.log(TAG + METHOD, "Intent has an extra: collectionName=" + collection);
            String previousIds = searchHandler.getString();
            searchHandler.restrictByCollection(collection);
            setTitle(collection);
            queryClickThroughs.add(new ClickThrough(this, collection, ClickThrough.QUERY_ADVANCED, previousIds, searchHandler.getString()));
            loadList();
        } else if (intent.hasExtra("authorName")) {
            String author = intent.getStringExtra("authorName");
            logger.log(TAG + METHOD, "Intent has an extra: authorName=" + author);
            String previousIds = searchHandler.getString();
            searchHandler.restrictByAuthor(author);
            setTitle(author);
            queryClickThroughs.add(new ClickThrough(this, author, ClickThrough.QUERY_ADVANCED, previousIds, searchHandler.getString()));
            loadList();
        } else if (intent.hasExtra("downloadBooks")) {
            logger.log(TAG + METHOD, "Intent has an extra: downloadBooks");
            downloadBooks();
        } else {
            logger.log(TAG + METHOD, "Intent.getAction() = " + intent.getAction() + "... ignoring.");
            loadList();
        }
    }

    public void onPause () {
        super.onPause();
        _ids = searchHandler.getIds();
    }
    
//    public void onResume () {
//        super.onResume();
//        loadList();
//    }
    
    public void loadList() {
        String METHOD = ":loadList(): ";
        logger.log(TAG + METHOD, "start");
        cursor = searchHandler.getCursor();
        
        String sortBy = sharedPref.getString("sortBy", "date_entered");
        boolean sortReverse = false;
        if (sortBy.endsWith("_r")) {
            sortBy = sortBy.substring(0, sortBy.length() - 2);
            sortReverse = true;
        }
        
        if (cursor.getCount() == 0) {
            SearchHandler testSearchHandler = new SearchHandler(this);
            testSearchHandler.setIds();
            if (testSearchHandler.getIds().size() == 0) {
                Intent in = new Intent(this, LoginActivity.class);
                startActivity(in);
            }
        }
        String[] columnNames = new String[] {"title", "author2"};
        adapter = new BookListAdapter(this, columnNames, queryClickThroughs, sortBy, sortReverse);
        adapter.sortAdapter(columnNames, sortBy, sortReverse);
        getListView().setFastScrollEnabled(true);
        setListAdapter(adapter);
    }
    
    public void downloadBooks() {
        String METHOD = ":downloadBooks(): ";
        logger.log(TAG + METHOD, "start");
        new LTLoginDownload().execute(null, null, null);
    }

    @SuppressWarnings("unchecked")
    public void importData() {
        String METHOD = ":importData(): ";
        logger.log(TAG + METHOD, "start");
        
        Uri uri = intent.getData();
//        logger.log(TAG, "Intent contains uri=" + uri);

        // Date presentTime = Calendar.getInstance().getTime();
        // SimpleDateFormat dateFormatter = new SimpleDateFormat(
        // "yyyyMMddhhmmss");
        // String newTableName = "booksFrom" +
        // dateFormatter.format(presentTime);
        // logger.log(TAG, "Intended table name=" + newTableName);

//        logger.log(TAG, "Opening InputStreamReader for " + uri + "...");
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

//        logger.log(TAG, "Creating CSVReader...");
        try {
            inputStreamReader = new InputStreamReader(inputStream, "utf-16");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        
        CSVReader<String[]> csvReader = new CSVReaderBuilder<String[]>(
                inputStreamReader)
                .strategy(new CSVStrategy('\t', '\b', '#', true, true))
                .entryParser(new EntryParser()).build();

        logger.log(TAG, "Reading csvData...");
        List<String[]> csvData = null;
        try {
            csvData = csvReader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.log(TAG, "Successfully read csvData");

        ImportBooksTask task = new ImportBooksTask();
        task.execute(csvData);
    }

    public class EntryParser implements CSVEntryParser<String[]> {
        public String[] parseEntry(String... data) {
            return data;
        }
    }

    private class ImportBooksTask extends
            AsyncTask<List<String[]>, Void, String> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            String METHOD = ":ImportBooksTask:onPreExecute(): ";
            logger.log(TAG + METHOD, "start");
            
            dialog = new ProgressDialog(BookListActivity.this);
            dialog.setTitle("Importing books...");
            dialog.setMessage("This could take a few minutes.");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @SuppressWarnings("static-access")
        @Override
        protected String doInBackground(List<String[]>... csvDatas) {
            String METHOD = ".ImportBooksTask:doInBackground()";
            logger.log(TAG + METHOD, "start");
            
            List<String[]> csvData = csvDatas[0];
            String newTableName = "books";
            logger.log(TAG, "Opening " + newTableName + " in internal database...");
            DbHelperNew dbHelper = new DbHelperNew(getApplicationContext());
            dbHelper.open();
            dialog.setMax(csvData.size());
            dbHelper.Db.beginTransaction();
            try {
                for (int i = 0; i < csvData.size(); i++) {
                    String[] csvRow = csvData.get(i);
                    String[] csvRowShort = new String[csvRow.length - 1];
                    for (int j = 0; j < (csvRowShort.length); j++) {
                        csvRowShort[j] = csvRow[j];
                    }
//                    for (int j = 0; j < csvRowShort.length; j++)
//                        Log.d(TAG + METHOD, "i=" + i + " j=" + j + " value=" + csvRowShort[j]);
                    dbHelper.addRow(csvRowShort);
                    dbHelper.Db.yieldIfContendedSafely();
                    dialog.setProgress(i);
                }
                dbHelper.Db.setTransactionSuccessful();
            } finally {
                dbHelper.Db.endTransaction();
            }
            dbHelper.close();

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            String METHOD = ":ImportBooksTask:onPostExecute(" + result + "): ";
            logger.log(TAG + METHOD, "start");
            
            //dialog.dismiss();
            Intent i = new Intent(getApplicationContext(), BookListActivity.class);
            finish();
            startActivity(i);
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
            goToPreferences();
            return true;
        case R.id.menuSearch:
            this.onSearchRequested();
            return true;
        case R.id.menuDelete:
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Delete books")
                    .setMessage(
                            "Are you sure you want to delete all your books?\n\n(This is only for data imported into LibraryThing Browser on your device, and cannot affect your LibraryThing account).")
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    String tableName = "books";
                                    DbHelperNew dbHelper = new DbHelperNew(
                                            getApplicationContext());
                                    dbHelper.delete();
                                    loadList();
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
            downloadBooks();
            return true;
        case R.id.menuReviews:
            startActivityWithIds(new Intent(this, ReviewListActivity.class));
            return true;
        case R.id.menuSortBy:
            String sortBy = sharedPref.getString("sortBy", "date_entered");
            String[] rawChoices = getResources().getStringArray(R.array.sort_by_raw);
            int defaultItem = 0;
            for (int i = 0; i < rawChoices.length; i++) {
                if (rawChoices[i].contains(sortBy)) {
                    defaultItem = i;
                    break;
                }
            }
            new AlertDialog.Builder(this)
                           .setSingleChoiceItems(getResources().getStringArray(R.array.sort_by_readable), defaultItem, null)
                           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int whichButton) {
                                   int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                   dialog.dismiss();
                                   String[] sortChoices = getResources().getStringArray(R.array.sort_by_raw);
                                   prefsEdit = sharedPref.edit();
                                   prefsEdit.putString("sortBy", sortChoices[selectedPosition]);
                                   prefsEdit.commit();
                                   loadList();
                               }
                           })
                           .show();
            return true;
        case R.id.menuComments:
            startActivityWithIds(new Intent(this, CommentListActivity.class));
            return true;
//        case R.id.menu_advanced_search:
//            Intent intent = new Intent(this, AdvancedSearchActivity.class);
//            intent.putExtra("ids", searchHandler.getString());
//            intent.putExtra("whereFrom", "menu");
//            startActivity(intent);
//            return true;
        default:
            return false;
        }
    }
    
    public void startActivityWithIds (Intent intent) {
        intent.putExtra("ids", searchHandler.getString());
        startActivity(intent);
    }

    public void importBooksFromDownload(String downloadedContent) {
        InputStream is = new ByteArrayInputStream(downloadedContent.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        CSVReader<String[]> csvReader = new CSVReaderBuilder<String[]>(
                br)
                .strategy(new CSVStrategy('\t', '\b', '#', true, true))
                .entryParser(new EntryParser()).build();

        logger.log(TAG, "Reading downloadedContent...");
        List<String[]> csvData = null;
        try {
            csvData = csvReader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.log(TAG, "Successfully read downloadedContent");

        ImportBooksTask task = new ImportBooksTask();
        task.execute(csvData);
        
        prefsEdit = sharedPref.edit();
        String s = SimpleDateFormat.getDateTimeInstance().format(new Date());
        prefsEdit.putString("last_download_summary", "Most recent: " + s);
        prefsEdit.commit();
    }
    
    private class LTLoginDownload extends AsyncTask<Boolean, Integer, String> {
        String result;        
        ProgressDialog dialog;

        
        @Override
        protected void onPreExecute() {
            String METHOD = ":LTLoginDownload:onPreExecute(): ";
            logger.log(TAG + METHOD, "start");
            
            
            
            dialog = new ProgressDialog(BookListActivity.this);
            dialog.setTitle("Downloading library...");
            dialog.setMessage("Logging in...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        
        protected void onProgressUpdate(Integer... progUpdate) {
            if (progUpdate[0] == PROGRESS_LOGGED_IN){  
               dialog.setMessage("Successfully logged in. Now downloading your library.");
               if (!sharedPref.getBoolean("lt_remember_credentials", false)) {
                   prefsEdit = sharedPref.edit();
                   prefsEdit.putString("lt_username", "");
                   prefsEdit.putString("lt_password", "");
                   prefsEdit.commit();
               }
           } else if (progUpdate[0] == PROGRESS_SUCCESS) {
               dialog.setMessage("Successfull! Let's import your books.");
           } else if (progUpdate[0] == PROGRESS_LOGIN_FAIL) {
               dialog.setMessage("Login failed.");
               dialog.dismiss();
               Intent in = new Intent(getApplicationContext(), LoginActivity.class);
               startActivity(in);
           }
        }
        
        protected String doInBackground(Boolean... bools) {
            String METHOD = ".LTLoginDownload.doInBackground()";
            logger.log(TAG + METHOD, "start");
            
            HttpClient client = new DefaultHttpClient();
            HttpPost loginPost = new HttpPost(
                    "http://www.librarything.com/enter/start");

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs
            .add(new BasicNameValuePair("formusername", sharedPref.getString("lt_username", "")));
            nameValuePairs.add(new BasicNameValuePair("formpassword",
                    sharedPref.getString("lt_password", "")));
            nameValuePairs.add(new BasicNameValuePair("index_signin_already",
                    "Sign in"));
            try {
                loginPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                        HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            HttpResponse loginResponse = null;
            try {
                loginResponse = client.execute(loginPost, localContext);
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            String loginResponseBody = "";
            try {
                loginResponseBody = EntityUtils.toString(loginResponse.getEntity(), HTTP.UTF_8);
            } catch (ParseException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            logger.log(TAG + METHOD, "loginResponseBody=" + loginResponseBody);
            if (!loginResponseBody.contains("Your collections")) {
                this.publishProgress(PROGRESS_LOGIN_FAIL);
                this.cancel(true);
                loginPost.abort();
            } else {
                this.publishProgress(PROGRESS_LOGGED_IN);
            }
            
            HttpGet downloadRequest = new HttpGet(
                    "http://www.librarything.com/export-tab");
            HttpResponse downloadResponse = null;
            try {
                downloadResponse = client.execute(downloadRequest,
                        localContext);
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            logger.log(TAG + METHOD, "downloadResponse StatusLine=" + downloadResponse.getStatusLine().toString());
            String downloadResponseBody = "";
            try {
                downloadResponseBody = EntityUtils.toString(downloadResponse
                        .getEntity(), HTTP.UTF_16);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            logger.log(TAG + METHOD, "downloadResponseBody=" + downloadResponseBody);
            result = downloadResponseBody;
            DbHelperNew dbHelper = new DbHelperNew(getApplicationContext());
            dbHelper.delete();
            this.publishProgress(PROGRESS_SUCCESS);
            return "";

        }

        protected void onPostExecute(String r) {
            String METHOD = ".LTLoginDownload.onPostExecute()";
            logger.log(TAG + METHOD, "start");
            if (result.length() > 0) {
                logger.log(TAG + METHOD, "importing books");
                importBooksFromDownload(result);
            } else {
                logger.log(TAG + METHOD, "the download was empty");
            }
        }

    }
    
    public void goToPreferences() {
        String METHOD = ".goToPreferences()";
        logger.log(TAG + METHOD, "opening application settings");
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
    }
    
    public class BookListAdapter extends BaseAdapter {

        static final int TYPE_CLICK_THROUGH =       0;
        static final int TYPE_RESULT_DETAIL =       1;
        static final int TYPE_SEPARATOR =           2;
        static final int TYPE_BOOK_LIST_ITEM =      3;
        
        static final int DETAIL_SEARCH =            0;
        static final int DETAIL_RESULTS =           1;
        
        int TYPES = 4;
        
        Context context;
        LayoutInflater inflater;
        ArrayList<ArrayList<String>> columns = new ArrayList<ArrayList<String>>();
        ArrayList<ClickThrough> clickThroughs = new ArrayList<ClickThrough>();
        
        public BookListAdapter (Context context, String[] columnNames, ArrayList<ClickThrough> clickThroughs, String sortBy, boolean sortReverse) {
            String METHOD = ".BookListAdapter(clickThroughs.size()=" + clickThroughs.size() + ")";
            this.context = context;
            inflater = LayoutInflater.from(context);
            columns.add(new ArrayList<String>(Arrays.asList(searchHandler.getString().split(","))));
            columns.add(searchHandler.getColumnArray(sortBy));
            for (int i = 0; i < columnNames.length; i++) {
                columns.add(searchHandler.getColumnArray(columnNames[i]));
            }
            this.sortAdapter(columnNames, sortBy, sortReverse);
//            logger.log(TAG + METHOD, "TYPES=" + TYPES);
        }
        
        public void sortAdapter (String[] columnNames, String sortBy, boolean sortReverse) {
            ArrayList<SearchHandler.BookProperty> books = new ArrayList<SearchHandler.BookProperty>();
            for (int i = 0; i < columns.get(0).size(); i++) {
                ArrayList<String> otherData = new ArrayList<String>();
                for (int j = 0; j < columnNames.length; j++) {
                    otherData.add(columns.get(j + 2).get(i));
                }
                books.add(new SearchHandler.BookProperty(Integer.valueOf(columns.get(0).get(i)), columns.get(1).get(i), otherData));
            }
            if (sortBy.startsWith("date_"))
                Collections.sort(books, new SearchHandler.DateComparator(sortReverse));
            else
                Collections.sort(books, new SearchHandler.StringComparator(sortReverse));
            ArrayList<Integer> sortedIds = new ArrayList<Integer>();
            for (int i = 0; i < books.size(); i++) {
                Integer id = books.get(i).id;
                sortedIds.add(id);
                columns.get(0).set(i, id.toString());
                columns.get(1).set(i, books.get(i).property);
                for (int j = 0; j < columnNames.length; j++) {
                    columns.get(j + 2).set(i, books.get(i).otherData.get(j));
                }
            }
            searchHandler.setIds(sortedIds);
            if (clickThroughs.size() > 0)
                TYPES += 1;
            this.clickThroughs = clickThroughs;
        }
        
        public int getCount() {
            String METHOD = ".getCount()";
            int count = columns.get(0).size() + 1 + 1 + clickThroughs.size();
//            logger.log(TAG + METHOD, "count=" + count);
            return count;
        }
        
        public int getViewTypeCount() {
            return TYPES;
        }
        
        public int getItemViewType(int position) {
            int viewType;
            if (position < clickThroughs.size())
                viewType = TYPE_CLICK_THROUGH;
            else if (position < clickThroughs.size() + 1)
                viewType = TYPE_RESULT_DETAIL;
            else if (position < clickThroughs.size() + 1 + 1)
                viewType = TYPE_SEPARATOR;
            else 
                viewType = TYPE_BOOK_LIST_ITEM;
            return viewType;
        }
        
        public int getBookListPosition(int position) {
            // The -1 is for the result details; -1 for the separator.
            return position - clickThroughs.size() - 1 - 1;
        }
        
        public int getWhichResultDetail(int position) {
            int viewType = -1;
            if (position == (clickThroughs.size()))
                viewType = DETAIL_RESULTS;
            return viewType;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String METHOD = ".getView(position=" + position + ")";
            ViewHolder holder = null;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case TYPE_CLICK_THROUGH:
//                        logger.log(TAG + METHOD, "convertView==null, type=TYPE_CLICK_THROUGH");
                        convertView = inflater.inflate(R.layout.book_list_click_through, null);
                        holder.textview = (TextView) convertView.findViewById(R.id.book_list_click_through_text);
                        break;
                    case TYPE_RESULT_DETAIL:
//                        logger.log(TAG + METHOD, "convertView==null, type=TYPE_RESULT_DETAIL");
                        convertView = inflater.inflate(R.layout.book_list_result_detail, null);
                        holder.title = (TextView) convertView.findViewById(R.id.book_list_result_detail_text);
                        break;
                    case TYPE_SEPARATOR:
//                        logger.log(TAG + METHOD, "convertView==null, type=TYPE_SEPARATOR");
                        convertView = inflater.inflate(R.layout.list_separator, null);
                        holder.view = convertView.findViewById(R.id.list_separator_view);
                        break;
                    case TYPE_BOOK_LIST_ITEM:
//                        logger.log(TAG + METHOD, "convertView==null, type=TYPE_BOOK_LIST_ITEM");
                        convertView = inflater.inflate(R.layout.book_list_item, null);
                        holder.title = (TextView) convertView.findViewById(R.id.book_list_item_title);
                        holder.subtitle = (TextView) convertView.findViewById(R.id.book_list_item_subtitle);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            switch (type) {
                case TYPE_CLICK_THROUGH:
                    ClickThrough clickThrough = clickThroughs.get(position);
                    holder.textview.setText(clickThrough.getViewText());
                    break;
                case TYPE_RESULT_DETAIL:
                    switch (getWhichResultDetail(position)) {
                        case DETAIL_SEARCH:
                            holder.title.setText("searched for something or other");
                            break;
                        case DETAIL_RESULTS:
                            holder.title.setText(columns.get(0).size() + " results:");
                            break;
                    }
                    break;
                case TYPE_BOOK_LIST_ITEM:
                    holder.title.setText(FormatText.asHtml(columns.get(2).get(getBookListPosition(position))));
                    holder.subtitle.setText(FormatText.asHtml(columns.get(3).get(getBookListPosition(position))));
                    break;
            }
            return convertView;
        }
    }
    
    @Override
    public void onListItemClick(ListView listView, View view, int position,
            long id) {
        String METHOD = ".onListItemClick(position=" + position + ")";
        super.onListItemClick(listView, view, position, id);
        int viewType = adapter.getItemViewType(position);
        if (viewType == adapter.TYPE_BOOK_LIST_ITEM) {
            String bookId = adapter.columns.get(0).get(adapter.getBookListPosition(position));
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra("_id", bookId);
            intent.putExtra("ids", searchHandler.getString());
            startActivity(intent);
        } else if (viewType == adapter.TYPE_CLICK_THROUGH) {
            logger.log(TAG + METHOD, "clicked on TYPE_CLICK_THROUGH");
            startActivity(adapter.clickThroughs.get(position).getIntent());
        }
    }
    
    static class ViewHolder {
        View view;
        TextView textview;
        TextView title;
        TextView subtitle;
        int position;
    }
    
    static class ClickThrough {
        static final int QUERY_TAG = 0;
        static final int QUERY_COLLECTION = 1;
        static final int QUERY_AUTHOR = 2;
        static final int QUERY_GENERAL = 4;
        static final int QUERY_ADVANCED = 5;
        
        int queryType;
        Context context; 
        String query;
        String allIds;
        String subsetIds;
        
        public ClickThrough (Context context, String query, int queryType, String allIds, String subsetIds) {
            this.context = context;
            this.queryType = queryType;
            this.query = query;
            this.allIds = allIds;
            this.subsetIds = subsetIds;
        }
        
        public String getViewText () {
            String text = "";
            switch (queryType) {
            case QUERY_ADVANCED:
                text = "Advanced search...";
                break;
            case QUERY_GENERAL:
                text = "Search for " + query;
                break;
            case QUERY_TAG:
                text = "Search for the tag '" + query + "'";
                break;
            case QUERY_COLLECTION:
                text = "Search for collection '" + query + "'";
                break;
            case QUERY_AUTHOR:
                text = "Search '" + query + "' in author names";
                break;
            }
            return text;
        }
        
        public Intent getIntent () {
            Intent intent = new Intent();
            if (queryType == QUERY_ADVANCED)
                intent.setClass(context, AdvancedSearchActivity.class);
            else 
                intent.setClass(context, BookListActivity.class);
            switch (queryType) {
                case QUERY_GENERAL:
                    intent.setAction(Intent.ACTION_SEARCH);
                    intent.putExtra("generalQuery", query);
                    break;
                case QUERY_TAG:
                    intent.putExtra("tagName", query);
                    break;
                case QUERY_COLLECTION:
                    intent.putExtra("collectionName", query);
                    break;
                case QUERY_AUTHOR:
                    intent.putExtra("authorName", query);
                    break;
                case QUERY_ADVANCED:
                    intent.putExtra("query", query);
                    intent.putExtra("allIds", allIds);
                    intent.putExtra("subsetIds", subsetIds);
                    intent.putExtra("whereFrom", "searchQuery");
                    break;
            }
            return intent;
        }
    }
}
