package com.example.sakura.websocketrxjava

import android.app.Activity
import android.app.Application

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object: GroundListener() {
            override fun onForeground(activity: Activity?) {
                if (isNeedWS(activity)) {
                    WebSocketWrapper.getInstance(WebSocketConfiguration.Builder()
                            .url(BuildConfig.URL)
                            .id(1)
                            .token("1")
                            .build())
                            .apply { connect(WebSocketPriority.BACKGROUND) }
                }
            }

            override fun onBackground(activity: Activity?) {
                if (isNeedWS(activity)) {
                    WebSocketWrapper.getInstance(WebSocketConfiguration.Builder()
                            .url(BuildConfig.URL)
                            .id(1)
                            .token("1")
                            .build())
                            .apply { disconnect(WebSocketPriority.BACKGROUND) }
                }
            }
        })
    }

    fun isNeedWS(activity: Activity?) : Boolean {
        activity?.takeIf { it !is SplashActivity }?.apply { return true }
        return false
    }
}