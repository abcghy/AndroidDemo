package com.example.sakura.websocketrxjava

import android.app.Activity
import android.app.Application

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object: GroundListener() {
            override fun onForeground(activity: Activity?) {
                WebSocketWrapper.getInstance(WebSocketConfiguration.Builder()
                        .url("ws://192.168.10.1:8999")
                        .id(1)
                        .build())
            }

            override fun onBackground(activity: Activity?) {

            }
        })
    }
}