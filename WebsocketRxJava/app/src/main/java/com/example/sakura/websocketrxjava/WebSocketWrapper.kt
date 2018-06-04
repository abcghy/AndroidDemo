package com.example.sakura.websocketrxjava

import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import okhttp3.*
import okio.ByteString
import java.net.SocketException
import java.util.concurrent.TimeUnit

fun <T> assertNull(any: T?): T {
    if (any == null) {
        throw NullPointerException("Null!")
    }
    return any
}

class WebSocketConfiguration {
    var id: Int
    var url: String
    var token: String

    private constructor(builder: WebSocketConfiguration.Builder) {
        id = assertNull(builder.id)
        url = assertNull(builder.url)
        token = assertNull(builder.token)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other !is WebSocketConfiguration) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    class Builder {
        var id: Int? = null
        var url: String? = null
        var token: String? = null

        fun id(id: Int): Builder {
            this.id = id
            return this
        }

        fun url(url: String): Builder {
            this.url = url
            return this
        }

        fun token(token: String): Builder {
            this.token = token
            return this
        }

        fun build(): WebSocketConfiguration {
            return WebSocketConfiguration(this)
        }
    }
}

class WebSocketWrapper {
    // a set or a map
    companion object {
        val wsInstanceMap : HashMap<WebSocketConfiguration, WebSocketInstance> = HashMap()

        fun getInstance(config: WebSocketConfiguration): WebSocketInstance {
            wsInstanceMap[config].also { if (it != null) return it }

            val request = Request.Builder()
                    .url(config.url)
                    .build()
            val httpClient = OkHttpClient.Builder()
                    .build()

            return WebSocketInstance.Builder()
                    .request(request)
                    .httpClient(httpClient)
                    .build()
                    .also { wsInstanceMap[config] = it }
        }

        fun clear() {
            for (wsInstance in wsInstanceMap) {
//                wsInstance.value.disconnect()
                wsInstance.value.clear()
            }
            wsInstanceMap.clear()
        }
    }
}

enum class WebSocketStatus {
    NOT_CONNECTED,
    CONNECTING,
    CONNECTED
}

/**
 * When you're logOut, whenever the other is change, do nothing.
 * When you're in background, whenever the other is change, do nothing
 * When your network is not good, whenever the other is change, do nothing
 */
enum class WebSocketPriority {
    LOG, // true logged in, false logged out
    BACKGROUND, // true in the foreground, false in background
    NETWORK, // true has network, false not has network
    RETRY // wait for later
}

/**
 * 需要自己维护状态
 */
class WebSocketInstance {
    var request: Request
    var httpClient: OkHttpClient

    private var retryDisposable: Disposable? = null

    var listener: WebSocketListener = object: WebSocketListener() {
        override fun onOpen(webSocket: WebSocket?, response: Response?) {
            super.onOpen(webSocket, response)

            websocketStatus = WebSocketStatus.CONNECTED

            Log.d("test", "onOpen")
        }

        override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
            super.onFailure(webSocket, t, response)
            websocketStatus = WebSocketStatus.NOT_CONNECTED

            Log.d("test", "onFailure $t")

            if (t !is SocketException) {
                retryDisposable?.dispose()

                // 重连
                retryDisposable = Observable.intervalRange(0, 3, 5, 5, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        // todo
                        .subscribe { connect(WebSocketPriority.RETRY) }
            }
        }

        override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
            super.onClosing(webSocket, code, reason)

            Log.d("test", "onClosing() code: $code")
            // My company use 4001 for auth failed, you may have another condition
            if (code == 4001) {
                // refresh token by sync, which is acquire the newest authorization
                // this is child thread, so not gonna block UI
                // refreshToken();

                // retry again
                connect(WebSocketPriority.RETRY)
            }
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            super.onMessage(webSocket, text)

            Log.d("test", "onMessage() text: $text")
        }

        override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
            super.onMessage(webSocket, bytes)
        }

        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
            super.onClosed(webSocket, code, reason)

            websocketStatus = WebSocketStatus.NOT_CONNECTED

            Log.d("test", "onClosed() code: $code")
        }
    }

    var websocket: WebSocket? = null

    var priorityMap: HashMap<WebSocketPriority, Boolean> = HashMap()

    var websocketStatus: WebSocketStatus = WebSocketStatus.NOT_CONNECTED
    set(value) {
        field = value
        Log.d("test", "websocket stats: $value")
    }

    constructor(builder: WebSocketInstance.Builder) {
        this.request = assertNull(builder.request)
        this.httpClient = assertNull(builder.httpClient)
//        this.listener = builder.listener

        // priorityMap init
        for (priority in WebSocketPriority.values()) {
            when (priority) {
                WebSocketPriority.LOG -> priorityMap[priority] = false
                WebSocketPriority.BACKGROUND -> priorityMap[priority] = true
                WebSocketPriority.NETWORK -> priorityMap[priority] = true
            }
        }
    }

    fun connect(priority: WebSocketPriority) {
        Log.d("test", "connect: $priority")
        when (priority) {
            WebSocketPriority.LOG -> {
                // 先标记我已经登录
                priorityMap[priority] = true
                if (priorityMap[WebSocketPriority.NETWORK] == true) {
                    // 连接
                    realConnect()
                }
            }
            WebSocketPriority.BACKGROUND -> {
                priorityMap[priority] = true
                if (priorityMap[WebSocketPriority.LOG] == true && priorityMap[WebSocketPriority.NETWORK] == true) {
                    realConnect()
                }
            }
            WebSocketPriority.NETWORK -> {
                priorityMap[priority] = true
                if (priorityMap[WebSocketPriority.LOG] == true && priorityMap[WebSocketPriority.BACKGROUND] == true) {
                    realConnect()
                }
            }
            // todo
            WebSocketPriority.RETRY -> {
                if (priorityMap[WebSocketPriority.LOG] == true &&
                        priorityMap[WebSocketPriority.BACKGROUND] == true &&
                        priorityMap[WebSocketPriority.NETWORK] == true) {
                    realConnect()
                }
            }
        }
    }

    fun disconnect(priority: WebSocketPriority) {
        Log.d("test", "disconnect: $priority")
        when (priority) {
            WebSocketPriority.LOG -> {
                // 先标记已经退出登录
                priorityMap[priority] = false
                // 无论状态如何，disconnect 并且清理 ws
                realDisconnect()
//                clear()
            }
            WebSocketPriority.BACKGROUND -> {
                priorityMap[priority] = false
                realDisconnect()
            }
            WebSocketPriority.NETWORK -> {
                priorityMap[priority] = false
                if (priorityMap[WebSocketPriority.BACKGROUND] == true) {
                    // 前台
                    realDisconnect()
                }
            }
        }
    }

    private fun realConnect() {
        if (websocketStatus == WebSocketStatus.CONNECTED || websocketStatus == WebSocketStatus.CONNECTING) {
            // it's already connected or it's connecting, no need to connect multiple times
            return
        }
        websocketStatus = WebSocketStatus.CONNECTING

        websocket = httpClient.newWebSocket(request, assertNull(listener))
    }

    private fun realDisconnect() {
        if (websocketStatus == WebSocketStatus.NOT_CONNECTED) {
            return
        }
//        Log.d("test", "close results: ${websocket?.close(1000, "Normal Close")}" )
        websocket?.cancel()
    }

    fun send(msg: String) {
        websocket?.send(msg)
    }

    fun clear() {
        websocket = null
    }

    class Builder {
        var request: Request? = null
        var httpClient: OkHttpClient? = null

        fun request(request: Request): Builder {
            this.request = request
            return this
        }

        fun httpClient(httpClient: OkHttpClient): Builder {
            this.httpClient = httpClient
            return this
        }

        fun build(): WebSocketInstance {
            return WebSocketInstance(this)
        }
    }
}