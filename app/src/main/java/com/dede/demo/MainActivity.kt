package com.dede.demo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.dede.weexlib.WeexActivity
import com.dede.weexlib.WeexLib

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun toWeexPage(v: View) {
        val intent = Intent(this, WeexActivity::class.java)
        intent.putExtra(WeexLib.EXTRA_WEEX_URL, "http://192.168.1.8:8080/index.js")
        startActivity(intent)
    }
}
