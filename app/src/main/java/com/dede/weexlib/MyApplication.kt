package com.dede.weexlib

import android.app.Application
import com.car300.weexlib.WeexLib

/**
 * Created by hsh on 2019/1/8 5:58 PM
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WeexLib.with(this)
            .setImageAdapter { url, view, quality, strategy -> }
            .init()
    }
}