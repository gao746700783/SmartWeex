package com.dede.demo

import android.app.Application
import com.smart.common.weex.WXCommonManager

/**
 * Created by hsh on 2019/1/8 5:58 PM
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        WXCommonManager.getInstance().initWeex(this)
        WXCommonManager.getInstance().isDebug = true

    }
}