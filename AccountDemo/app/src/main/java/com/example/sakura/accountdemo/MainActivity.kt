package com.example.sakura.accountdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    var realm: Realm? = null
    var disposable: CompositeDisposable = CompositeDisposable()

//    var thread1: Thread? = null
//    var thread2: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        thread1 = Thread()
//        thread2 = Thread()

        realm = Realm.getDefaultInstance()

        realm!!.where(Person::class.java)
                .findAll()
                .asFlowable()
                .subscribe { results ->
                    Log.d("test", "tv_main get")
                    tv_main.text = results.size.toString()
                }
                .apply { disposable.add(this) }

        btn_main.setOnClickListener {
            realm!!.executeTransaction {
                val person = Person("Huiyu", "Gao")
                Log.d("test", "btn_main add")
                it.insertOrUpdate(person)
            }
        }

        val ioWorker = Schedulers.io().createWorker()
        ioWorker.schedule {
            val realm = Realm.getDefaultInstance()

            Flowable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.io())
                    .subscribe {
                        Log.d("test", "thread: ${Thread.currentThread().name}")
                        val count = realm.where(Person::class.java)
                                .count()
                        runOnUiThread {
                            tv_child_1.text = count.toString()
                        }
                    }
        }

        btn_child_1.setOnClickListener {
            ioWorker.schedule {
                var realm = Realm.getDefaultInstance()
                realm!!.executeTransaction {
                    val person = Person("Huiyu", "Gao")
                    Log.d("test", "btn_child_1 add thread: ${Thread.currentThread().name}")
                    it.insertOrUpdate(person)
                }
                realm.close()
            }
        }
    }

    override fun onDestroy() {
        disposable.clear()
        realm?.close()

        super.onDestroy()
    }
}
