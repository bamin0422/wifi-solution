package com.CVproject.wifi_solution.Adapter


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.CVproject.wifi_solution.MainActivity
import com.CVproject.wifi_solution.R


class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        startLoading()
    }

    private fun startLoading() {
        val handler = Handler()
        handler.postDelayed(Runnable {
            val intent  = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        },2000)
    }
}