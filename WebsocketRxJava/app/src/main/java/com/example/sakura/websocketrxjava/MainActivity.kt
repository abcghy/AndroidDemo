package com.example.sakura.websocketrxjava

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import okio.ByteString

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val instance = WebSocketWrapper.getInstance(
                WebSocketConfiguration.Builder()
                        .url(BuildConfig.URL)
                        .id(1)
                        .build()
        )

        instance.listener = object: WebSocketListener() {
            override fun onOpen(webSocket: WebSocket?, response: Response?) {
                super.onOpen(webSocket, response)

                Log.d("test", "onOpen")
            }

            override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
                super.onFailure(webSocket, t, response)

                Log.d("test", "onFailure $t")
            }

            override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
                super.onClosing(webSocket, code, reason)

                Log.d("test", "onClosing() code: $code")
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

                Log.d("test", "onClosed() code: $code")
            }
        }
        instance.connect()

        button.setOnClickListener {
            instance.disconnect()
            startActivity(Intent(this@MainActivity, SplashActivity::class.java))
            finish()
        }

        editText.setOnEditorActionListener { v, actionId, event ->
            instance.send(v.text.toString())
            true
        }
    }
}
