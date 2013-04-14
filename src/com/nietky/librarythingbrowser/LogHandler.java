package com.nietky.librarythingbrowser;

import android.content.SharedPreferences;
import android.util.Log;

public class LogHandler  {
    Integer maxLog;
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefsEdit;
    boolean logFlag;
    
    public LogHandler (SharedPreferences prefs) {
        sharedPref = prefs;
        logFlag = sharedPref.getBoolean("log_flag", false);
        maxLog = Integer.valueOf(sharedPref.getString("max_log", "1000000")); 
        prefsEdit = sharedPref.edit();
        String log = sharedPref.getString("debug_log", "");
        prefsEdit.putString("debug_log", log);
        prefsEdit.commit();
    }
    
    public void log (String tag, String message) {
        if (logFlag) {
            maxLog = Integer.valueOf(sharedPref.getString("max_log", "1000000")); 
            String log = sharedPref.getString("debug_log", "");
            log = log + "\n" + tag + " : " + message;
            int n = log.length();
            if (n > maxLog)
                log = log.substring(n - maxLog, n);
            prefsEdit.putString("debug_log", log);
            prefsEdit.commit();
            Log.d(tag, message);
        }
    }
    
}
