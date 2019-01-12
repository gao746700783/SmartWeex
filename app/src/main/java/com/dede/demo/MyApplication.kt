package com.dede.demo

import android.app.Application
import com.dede.weexlib.WeexLib

/**
 * Created by hsh on 2019/1/8 5:58 PM
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WeexLib.with(this)
            .debug(BuildConfig.DEBUG)
            .setImageAdapter { url, view, quality, strategy ->
                GlideApp.with(view).load(url).into(view)
            }
            .init()
    }
}