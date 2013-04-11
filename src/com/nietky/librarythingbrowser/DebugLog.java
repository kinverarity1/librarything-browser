package com.nietky.librarythingbrowser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DebugLog extends Activity {
    String logText;
    Integer maxLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_log);
        
        TextView text = (TextView) findViewById(R.id.debug_log_text);
        final EditText message = (EditText) findViewById(R.id.debug_log_message);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        maxLog = Integer.valueOf(sharedPref.getString("max_log", "1000000")); 
        logText = sharedPref.getString("debug_log", "");
        int n = logText.length();
        if (n > maxLog) {
            logText = logText.substring(n - maxLog, n);
            SharedPreferences.Editor prefsEdit = sharedPref.edit();
            prefsEdit.putString("debug_log", logText);
        }
        text.setText(logText.length() + " characters in log");
        
        Button button = (Button) findViewById(R.id.debug_log_button);
        button.setOnClickListener(new OnClickListener () {
            public void onClick(View arg0) {
                new SendLog(logText, message.getText().toString()).execute(true);
            }
        });
        Button clearButton = (Button) findViewById(R.id.debug_log_clear);
        clearButton.setOnClickListener(new OnClickListener () {
            public void onClick(View arg0) {
                SharedPreferences.Editor e = sharedPref.edit();
                e.remove("debug_log");
                e.commit();
                finish();
            }
        });
    }
    
    public void showLog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(logText);
        builder.setTitle("Debug log");
        AlertDialog alert = builder.create();
        alert.show();
        TextView msgTxt = (TextView) alert.findViewById(android.R.id.message);
        msgTxt.setTextSize((float) 14);
    }
    
    private class SendLog extends AsyncTask<Boolean, Integer, Boolean> {
        String log;
        String message;
        ProgressDialog dialog;
        public SendLog (String _log, String _message) {
            log = _log;
            message = _message;
        }
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(DebugLog.this);
            dialog.setTitle("Sending log");
            dialog.setMessage("sending");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        protected Boolean doInBackground(Boolean... bools) {
            HttpClient client = new DefaultHttpClient();
            HttpPost logPost = new HttpPost(
                    "http://gy19x0ai5l.appspot.com/ltb_debug_log");
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("log", log.replace("\n", "NEWLINE")));
            nameValuePairs.add(new BasicNameValuePair("message", message));
            try {
                logPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                        HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            HttpResponse sendLog = null;
            try {
                sendLog = client.execute(logPost);
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                return false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                return false;
            }
            return true;
        }
        protected void onPostExecute(Boolean success) {
            if (success) dialog.setMessage("successful");
            if (!success) dialog.setMessage("not successful");
            finish();
        }
    }
        
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.debug_log, menu);
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
