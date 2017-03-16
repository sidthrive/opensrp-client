package org.ei.opensrp.view.activity;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import org.ei.opensrp.Context;
import org.ei.opensrp.R;

import java.io.File;
import java.util.Locale;


public class DrishtiApplication extends Application {
    private static final String TAG = "DrishtiApplication";

    protected Locale locale = null;
    protected Context context;
    private static DrishtiApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public void logoutCurrentUser(){
        Log.e(TAG, "Child classes should implement this function");
    }

    public static synchronized DrishtiApplication getInstance() {
        return mInstance;
    }

    public static String getAppDir(){
        File appDir = DrishtiApplication.getInstance().getApplicationContext().getDir("opensrp", android.content.Context.MODE_PRIVATE); //Creating an internal dir;
        return appDir.getAbsolutePath();
    }

}