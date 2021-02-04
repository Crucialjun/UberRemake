package com.crucialtech.uberremake

import android.app.Notification
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        delaySplashScreen()
    }

    private fun delaySplashScreen() {
        Completable.timer(5,
            TimeUnit.SECONDS,
            AndroidSchedulers.mainThread()).subscribe(object : Action {
            override fun run() {
                Toast.makeText(this@MainActivity, "Splash Screen Done", Toast.LENGTH_SHORT).show()
            }

        })
    }
}