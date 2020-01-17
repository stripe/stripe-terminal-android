package com.stripe.example.javaapp;

import android.app.Application;
import android.os.StrictMode;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.facebook.stetho.Stetho;
import com.stripe.stripeterminal.TerminalLifecycleObserver;

public class StripeTerminalApplication extends Application {
    private final TerminalLifecycleObserver observer = TerminalLifecycleObserver.Companion.getInstance();

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
                        .penaltyDeath()
                        .build());

        super.onCreate();

        Stetho.initializeWithDefaults(this);

        registerActivityLifecycleCallbacks(observer);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(observer);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        observer.onTrimMemory(level, this);
    }
}
