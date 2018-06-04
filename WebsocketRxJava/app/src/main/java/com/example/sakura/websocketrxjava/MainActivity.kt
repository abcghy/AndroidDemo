package com.example.sakura.websocketrxjava

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import cn.gailvlun.gll.listener.ReachabilityReceiver
import kotlinx.android.synthetic.main.activity_main.*
import android.content.IntentFilter
import android.util.Log
import cn.gailvlun.gll.listener.ReachabilityEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity() {

    var reachabilityReceiver: ReachabilityReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)

        val instance = WebSocketWrapper.getInstance(
                WebSocketConfiguration.Builder()
                        .url(BuildConfig.URL)
                        .id(1)
                        .token("1")
                        .build()
        )
        instance.connect(WebSocketPriority.LOG)

        button.setOnClickListener {
            instance.disconnect(WebSocketPriority.LOG)
            startActivity(Intent(this@MainActivity, SplashActivity::class.java))
            finish()
        }

        editText.setOnEditorActionListener { v, actionId, event ->
            instance.send(v.text.toString())
            true
        }

        // 注册网络监测
        reachabilityReceiver = ReachabilityReceiver().also {
            registerReceiver(it, IntentFilter().apply {
                addAction("android.net.conn.CONNECTIVITY_CHANGE")
                addAction("android.net.wifi.WIFI_STATE_CHANGED")
                addAction("android.net.wifi.STATE_CHANGE") })
        }
    }

    /**
     * todo: When you switch from wifi to 4g, there will be a little not_connected time.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReachabilityEvent(reachabilityEvent: ReachabilityEvent) {
        Log.d("test", "reachabilityEvent: ${reachabilityEvent.reachabilityStatus}")
        when (reachabilityEvent.reachabilityStatus) {
            ReachabilityReceiver.NOT_CONNECTED -> {
                WebSocketWrapper.getInstance(WebSocketConfiguration.Builder()
                        .url(BuildConfig.URL)
                        .id(1)
                        .token("1")
                        .build())
                        .run { this.disconnect(WebSocketPriority.NETWORK) }
            }
            ReachabilityReceiver.MOBILE, ReachabilityReceiver.WIFI -> {
                WebSocketWrapper.getInstance(WebSocketConfiguration.Builder()
                        .url(BuildConfig.URL)
                        .id(1)
                        .token("1")
                        .build())
                        .run { this.connect(WebSocketPriority.NETWORK) }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(reachabilityReceiver)
        EventBus.getDefault().unregister(this)

        super.onDestroy()
    }
}
