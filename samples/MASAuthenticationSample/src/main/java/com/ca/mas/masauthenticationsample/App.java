package com.ca.mas.masauthenticationsample;

import android.app.Application;

import com.ca.mas.foundation.MAS;

/**
 * Created by wanja11 on 2018-01-23.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MAS.start(this);
    }
}
