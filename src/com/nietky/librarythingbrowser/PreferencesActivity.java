package com.nietky.librarythingbrowser;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity  {
    String TAG = "PreferencesActivity";
    LogHandler logger;
    
    SharedPreferences sharedPref;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        
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
        prefLastDownloaded.setSummary(sharedPref.getString("last_download_summary", ""));
        
        ListPreference prefSortBy = (ListPreference) findPreference("sortBy");
        prefSortBy.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateSortBySummary(true, (String) newValue);
                return true;
            }
        });
        updateSortBySummary(false, sharedPref.getString("sortBy", ""));
    }
    
    public void updateSortBySummary (boolean changed, String newKey) {
        
        ListPreference prefSortBy = (ListPreference) findPreference("sortBy");
        String sortBy = newKey;
        String sortByReadable = "";
        String[] rawChoices = getResources().getStringArray(R.array.sort_by_raw);
        String[] readableChoices = getResources().getStringArray(R.array.sort_by_readable);
        for (int i = 0; i < rawChoices.length; i++) {
            if (sortBy.matches(rawChoices[i])) {
                sortByReadable = readableChoices[i];
                break;
            }
        }
        String summary = sortByReadable;
        if (changed)
            summary += "\n\nChange applied on app restart.";
        prefSortBy.setSummary(summary);
    }
    

}

        
