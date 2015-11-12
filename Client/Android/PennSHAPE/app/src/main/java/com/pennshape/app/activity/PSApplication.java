package com.pennshape.app.activity;

import android.app.Application;

import com.pennshape.app.location.PSLocationManager;

/**
 * Created by hasun on 11/12/15.
 */
public class PSApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        PSLocationManager.sharedManager().setContext(getApplicationContext());
        PSLocationManager.sharedManager().getLocation();
    }
}
