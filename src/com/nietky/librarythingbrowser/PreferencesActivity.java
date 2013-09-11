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
    String TAG = "PreferencesActivity";
    SharedPreferences sharedPref;
    LogHandler logger;
    
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
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefLastDownloaded.setSummary(sharedPref.getString("last_download_summary", ""));
        
    }
    
    public void onResume () {
        super.onResume();
        
        String[] sort_by_raw = getResources().getStringArray(R.array.sort_by_raw);
        String[] sort_by_readable = getResources().getStringArray(R.array.sort_by_readable);
        String sort_order = sharedPref.getString("sortOrder", "_id");
        String pretty_sort_order = "None";
        for (int i = 0; i < sort_by_raw.length; i++) {
            if (sort_by_raw[i].contentEquals(sort_order)) {
                pretty_sort_order = sort_by_readable[i];
                break;
            }
        }
        Preference prefSortOrder = (Preference) findPreference("sortOrder");
        prefSortOrder.setSummary("Currently sorted by: " + pretty_sort_order);
    }

}

        
