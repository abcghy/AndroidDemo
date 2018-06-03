package cn.gailvlun.gll.listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import org.greenrobot.eventbus.EventBus

/**
 * Created by sakura on 2018/5/8.
 */
class ReachabilityReceiver : BroadcastReceiver() {

    companion object {
        const val NOT_CONNECTED = 0L
        const val MOBILE = 1L
        const val WIFI = 2L
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork : NetworkInfo? = manager.activeNetworkInfo

            if (activeNetwork == null) {
                EventBus.getDefault().post(ReachabilityEvent(NOT_CONNECTED))
                return
            }

            if (activeNetwork.isAvailable) {
                when (activeNetwork.type) {
                    ConnectivityManager.TYPE_WIFI -> EventBus.getDefault().post(ReachabilityEvent(WIFI))
                    ConnectivityManager.TYPE_MOBILE -> EventBus.getDefault().post(ReachabilityEvent(MOBILE))
                    else -> EventBus.getDefault().post(ReachabilityEvent(NOT_CONNECTED))
                }
            }
        }
    }
}

class ReachabilityEvent(val reachabilityStatus: Long)