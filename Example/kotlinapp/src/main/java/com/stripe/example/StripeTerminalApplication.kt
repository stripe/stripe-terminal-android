package com.stripe.example

import android.app.Application
import android.os.StrictMode
import androidx.lifecycle.ProcessLifecycleOwner
import com.facebook.stetho.Stetho
import com.stripe.stripeterminal.TerminalLifecycleObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StripeTerminalApplication : Application() {
    private val observer: TerminalLifecycleObserver = TerminalLifecycleObserver.getInstance()

    override fun onCreate() {
        // Should happen before super.onCreate()
        StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectAll()
                        .penaltyLog()
                        .build())

        StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .penaltyLog()
                        .penaltyDeath()
                        .build())

        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            Stetho.initializeWithDefaults(this@StripeTerminalApplication)
        }

        registerActivityLifecycleCallbacks(observer)
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        observer.onTrimMemory(level, this)
    }
}
