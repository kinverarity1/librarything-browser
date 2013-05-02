package com.nietky.librarythingbrowser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginActivity extends Activity {
    
    private static final String TAG = "LoginActivity";
    LogHandler logger;

    EditText usernameBox;
    EditText passwordBox;
    CheckBox rememberCredentials;
    
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor prefsEdit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String METHOD = ".onCreate()";
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        logger = new LogHandler(sharedPref);
        logger.log(TAG + METHOD, "Start");
        
        usernameBox = (EditText) findViewById(R.id.login_lt_username);
        passwordBox = (EditText) findViewById(R.id.login_lt_password);
        
        usernameBox.setText(sharedPref.getString("lt_username", ""));
        passwordBox.setText(sharedPref.getString("lt_password", ""));
        
        rememberCredentials = (CheckBox) findViewById(R.id.login_lt_remember_credentials);
        rememberCredentials.setChecked(sharedPref.getBoolean("lt_remember_credentials", false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuPreferences:
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        default:
            return false;
        }
    }
    
    public void downloadLibrary (View view) {
        String METHOD = ":downloadLibrary(): ";
        Log.d(TAG + METHOD, "start");
        
        String LTUsername = usernameBox.getText().toString();
        String LTPassword = passwordBox.getText().toString();
        
        prefsEdit = sharedPref.edit();
        prefsEdit.putBoolean("lt_remember_credentials", rememberCredentials.isChecked());
        Log.d(TAG + METHOD, "Credentials is checked or not?" + rememberCredentials.isChecked());
        prefsEdit.putString("lt_username", LTUsername);
        prefsEdit.putString("lt_password", LTPassword);
        prefsEdit.commit();
        
        Intent in = new Intent(this, BookListActivity.class);
        in.putExtra("downloadBooks", true);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
        finish();
    }
}
