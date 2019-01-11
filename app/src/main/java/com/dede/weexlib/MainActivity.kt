package com.dede.weexlib

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.car300.weexlib.WeexActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun toWeexPage(v: View) {
        startActivity(Intent(this, WeexActivity::class.java))
    }
}
