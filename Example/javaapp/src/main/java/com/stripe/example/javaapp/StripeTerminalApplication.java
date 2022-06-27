package com.stripe.example.javaapp;

import android.app.Application;
import android.os.StrictMode;

import com.stripe.stripeterminal.TerminalApplicationDelegate;

public class StripeTerminalApplication extends Application {
    @Override
    public void onCreate() {
        // Should happen before super.onCreate()
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectAll()
                        .penaltyLog()
                        .build());

        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .penaltyLog()
                        .build());

        super.onCreate();

        TerminalApplicationDelegate.onCreate(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        TerminalApplicationDelegate.onTrimMemory(this, level);
    }
}
