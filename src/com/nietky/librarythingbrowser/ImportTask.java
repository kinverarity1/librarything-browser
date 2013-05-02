package com.nietky.librarythingbrowser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;

public class ImportTask extends AsyncTask<Void, Integer, Void> {
    private final static String TAG = "ImportTask";
    
    Context context;
    HttpContext httpContext;
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefsEdit;
    ProgressDialog dialog;
    
    private final static int PROGRESS_LOGGED_IN = 3;
    private final static int PROGRESS_LOGIN_FAIL = 5;
    private final static int PROGRESS_DOWNLOAD_SUCCESS = 4;
    
    public ImportTask (Context context) {
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        CookieStore cookieStore = new BasicCookieStore();
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }
    
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setTitle("Downloading your library");
        dialog.setMessage("Logging in");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    
    protected class EntryParser implements CSVEntryParser<String[]> {
        public String[] parseEntry(String... data) {
            return data;
        }
    }
    
    protected Void doInBackground(Void... arg0) {
        String METHOD = ".doInBackground()";
        // Log in
        
        HttpClient client = new DefaultHttpClient();
        HttpPost loginPost = new HttpPost("http://www.librarything.com/enter/start");

        List<NameValuePair> nvp = new ArrayList<NameValuePair>(3);
        nvp.add(new BasicNameValuePair("formusername", sharedPref.getString("lt_username", "")));
        nvp.add(new BasicNameValuePair("formpassword", sharedPref.getString("lt_password", "")));
        nvp.add(new BasicNameValuePair("index_signin_already", "Sign in"));
        
        Log.d(TAG + METHOD, "Ready to send log in HTTP POST");
        try {
            loginPost.setEntity(new UrlEncodedFormEntity(nvp, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {}
        HttpResponse loginResponse = null;
        try {
            loginResponse = client.execute(loginPost, httpContext);
        } catch (ClientProtocolException e) {} catch (IOException e) {}
        String loginResponseBody = "";
        try {
            loginResponseBody = EntityUtils.toString(loginResponse.getEntity(), HTTP.UTF_8);
        } catch (ParseException e1) {} catch (IOException e1) {}
        
        // Check for successful login
        
        Log.d(TAG + METHOD, "Checking for successful login.");
        if (!loginResponseBody.contains("/home/" + sharedPref.getString("lt_username", ""))) {
            this.publishProgress(PROGRESS_LOGIN_FAIL);
            this.cancel(true);
            loginPost.abort();
        } else 
            this.publishProgress(PROGRESS_LOGGED_IN);
        
        // Download tab-delimited export file.
        
        HttpGet downloadRequest = new HttpGet("http://www.librarything.com/export-tab");
        HttpResponse downloadResponse = null;
        try {
            downloadResponse = client.execute(downloadRequest, httpContext);
        } catch (ClientProtocolException e) {} catch (IOException e) {}
        String downloadResponseBody = "";
        try {
            downloadResponseBody = EntityUtils.toString(downloadResponse.getEntity(), HTTP.UTF_16);
        } catch (ParseException e) {} catch (IOException e) {}
        this.publishProgress(PROGRESS_DOWNLOAD_SUCCESS);
        
        // Import books.
        
        InputStream is = new ByteArrayInputStream(downloadResponseBody.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        CSVReader<String[]> csvReader = new CSVReaderBuilder<String[]>(br)
                .strategy(new CSVStrategy('\t', '\b', '#', true, true))
                .entryParser(new EntryParser()).build();
        
        List<String[]> csvData = null;
        try {
            csvData = csvReader.readAll();
        } catch (IOException e) {}
        
        DbHelper dbHelper = new DbHelper(context.getApplicationContext());
        dbHelper.deleteTable();
        dbHelper.open();
        dialog.setMax(csvData.size());
        dbHelper.db.beginTransaction();
        Log.d(TAG + METHOD, "Transaction has begun.");
        try {
//            for (int i = 0; i < csvData.size(); i++) {
            for (int i = 0; i < 30; i++) {
                String[] csvRow = csvData.get(i);
                String[] data = new String[dbHelper.KEYS.length];
                data[0] = String.valueOf(i);
                for (int j = 1; j < data.length; j++)
                    data[j] = csvRow[j - 1];
                for (int j = 0; j < data.length; j++) {
                    Log.d(TAG + METHOD, "i=" + i + ", j=" + j + ", KEYS[" + j + "]=" + dbHelper.KEYS[j] + ", data[" + j + "]=" + data[j]);
                }
                dbHelper.addBookFromStringArrayDbUnopened(data);
//                dialog.setProgress(i);
            }
            dbHelper.db.setTransactionSuccessful();
            Log.d(TAG + METHOD, "Set transaction as successful.");
        } finally {
            dbHelper.db.endTransaction();
            Log.d(TAG + METHOD, "Transaction has ended.");
        }
        dbHelper.close();
        Log.d(TAG + METHOD, "dbHelper has been closed.");
        return null;
    }
    
    protected void onProgressUpdate(Integer... progress) {
        switch (progress[0]) {
            case (PROGRESS_LOGGED_IN):
                dialog.setMessage("Successfully logged in. Now downloading your library.");
                if (!sharedPref.getBoolean("lt_remember_credentials", false)) {
                    prefsEdit = sharedPref.edit();
                    prefsEdit.putString("lt_username", "");
                    prefsEdit.putString("lt_password", "");
                    prefsEdit.commit();
                }
                break;
            case (PROGRESS_DOWNLOAD_SUCCESS):
                dialog.setMessage("Successfully downloaded your books. Now importing them.");
                break;
            case (PROGRESS_LOGIN_FAIL):
                dialog.setMessage("Login failed.");
                dialog.dismiss();
                context.startActivity(new Intent(context, LoginActivity.class));
                break;
        }
    }

    protected void onPostExecute(Void result) {
        String METHOD = ".onPostExecute()";
        Log.d(TAG + METHOD, "Start");
        prefsEdit = sharedPref.edit();
        String s = SimpleDateFormat.getDateTimeInstance().format(new Date());
        prefsEdit.putString("last_download_summary", "Most recent: " + s);
        prefsEdit.commit();
        Intent intent = new Intent(context, BookListActivityNew.class);
        context.startActivity(intent);
        Log.d(TAG + METHOD, "New Activity launched.");
        dialog.dismiss();
        Log.d(TAG + METHOD, "Dialog dismissed.");
  }
}

