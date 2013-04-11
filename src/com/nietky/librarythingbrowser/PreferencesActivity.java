package com.nietky.librarythingbrowser;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity  {

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            //
        }
        String versionNumber = pInfo.versionName;
        int versionCode = pInfo.versionCode;
        
        Preference prefAboutVersion = (Preference) findPreference("about_version");
        prefAboutVersion.setTitle(getString(R.string.preferences_ui_version) + " " + versionNumber + "vc" + versionCode);
        
        Preference prefLastDownloaded = (Preference) findPreference("last_download");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefLastDownloaded.setSummary(sharedPrefs.getString("last_download_summary", ""));
        
//        DialogPreference debugLog = (DialogPreference) findPreference("debug_dialog_pref");
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
//        debugLog.setDialogMessage(sharedPref.getString("debug_log", "No log."));
//        
//
//        <DialogPreference
//            android:key="debug_dialog_pref"
//            android:dialogTitle="Log for debugging"
//            android:dialogMessage="The log goes here"
//            android:positiveButtonText="OK"
//            android:negativeButtonText="Cancel" />
    }
    

}

        
