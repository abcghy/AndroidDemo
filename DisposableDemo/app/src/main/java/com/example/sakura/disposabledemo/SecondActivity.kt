package com.example.sakura.disposabledemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class SecondActivity: AppCompatActivity() {

    private var disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_second)

        disposable.add(Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { Log.d("test", "$it") })
    }

    override fun onStart() {
        super.onStart()

        disposable.add(Observable.timer(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { Log.d("test", "$it") })
    }

    override fun onResume() {
        super.onResume()

        disposable.add(Observable.timer(4, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { Log.d("test", "$it") })
    }

    override fun onDestroy() {
        disposable.clear()

        super.onDestroy()
    }

}