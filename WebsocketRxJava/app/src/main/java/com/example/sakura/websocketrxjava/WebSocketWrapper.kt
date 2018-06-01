package com.example.sakura.websocketrxjava

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
            val wsInstance = wsInstanceMap[config]
            if (wsInstance != null) {
                return wsInstance
            }

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

        }
    }
}

class WebSocketInstance {
    var request: Request
    var httpClient: OkHttpClient
    var listener: WebSocketListener? = null

    var websocket: WebSocket? = null

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
        websocket = httpClient.newWebSocket(request, assertNull(listener))
    }

    fun disconnect() {
        websocket?.close(1000, "Normal Close")
    }

    fun send(msg: String) {
        websocket?.send(msg)
    }

    fun clear() {

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