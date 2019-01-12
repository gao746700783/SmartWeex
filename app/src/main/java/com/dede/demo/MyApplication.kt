package com.dede.demo

import android.app.Application
import android.text.TextUtils
import com.dede.weexlib.WeexLib
import com.taobao.weex.WXEnvironment

/**
 * Created by hsh on 2019/1/8 5:58 PM
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WeexLib.with(this)
            .debug(BuildConfig.DEBUG)
            .setImageAdapter { url, view, quality, strategy ->
                if (view?.layoutParams == null) {
                    return@setImageAdapter
                }
                if (TextUtils.isEmpty(url)) {
                    view.setImageBitmap(null)
                    return@setImageAdapter
                }
                GlideApp.with(WXEnvironment.getApplication())
                    .load(url)
                    .into(view)
            }
            .init()
    }
}