package com.applozic.mobicomkit.uiwidgets;

import android.app.Application;

/**
 * Created by devashish on 28/4/14.
 */
public class MobiComKitApplication extends Application {

    public static final String TITLE = "Conversations";

    @Override
    public void onCreate() {
        // workaround for http://code.google.com/p/android/issues/detail?id=20915
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
        }
        super.onCreate();
    }
}