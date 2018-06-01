package com.example.sakura.websocketrxjava

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

fun <T> assertNull(any: T?): T {
    if (any == null) {
        throw NullPointerException("Null!")
    }
    return any
}

class WebSocketConfiguration {
    var id: Int
    var url: String

    private constructor(builder: WebSocketConfiguration.Builder) {
        id = assertNull(builder.id)
        url = assertNull(builder.url)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other !is WebSocketConfiguration) {
            return false
        }
        return id == other.id && url == other.url
    }

    override fun hashCode(): Int {
        return id.hashCode() + url.hashCode()
    }

    class Builder {
        var id: Int? = null
        var url: String? = null

        fun id(id: Int): Builder {
            this.id = id
            return this
        }

        fun url(url: String): Builder {
            this.url = url
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
 * 需要自己维护状态
 */
class WebSocketInstance {
    var request: Request
    var httpClient: OkHttpClient
    var listener: WebSocketListener? = null

    var websocket: WebSocket? = null

    private var _lastTimeConnect: Long = 0L
    private var _lastTimeDisconnect: Long = 0L
    var websocketStatus: WebSocketStatus = WebSocketStatus.NOT_CONNECTED
//    set(value) {
//        field = value
//    }

    constructor(builder: WebSocketInstance.Builder) {
        this.request = assertNull(builder.request)
        this.httpClient = assertNull(builder.httpClient)
        this.listener = builder.listener
    }

    constructor(request: Request, httpClient: OkHttpClient) {
        this.request = request
        this.httpClient = httpClient
    }

    fun connect() {
//        if (System.currentTimeMillis() - _lastTimeConnect < 5000) {
//             不能连接，太早了
//        }
//
//        _lastTimeConnect = System.currentTimeMillis()
        realConnect()
    }

    private fun realConnect() {
        websocket = httpClient.newWebSocket(request, assertNull(listener))
    }

    fun disconnect() {
        realDisconnect()
    }

    private fun realDisconnect() {
        websocket?.close(1000, "Normal Close")
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
        var listener: WebSocketListener? = null

        fun request(request: Request): Builder {
            this.request = request
            return this
        }

        fun httpClient(httpClient: OkHttpClient): Builder {
            this.httpClient = httpClient
            return this
        }

        fun listener(listener: WebSocketListener): Builder {
            this.listener = listener
            return this
        }

        fun build(): WebSocketInstance {
            return WebSocketInstance(this)
        }
    }
}