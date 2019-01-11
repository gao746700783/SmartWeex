package com.car300.weexlib;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hsh on 2019/1/9 5:00 PM
 */
class Preferences {

    private static Preferences instance;

    static synchronized Preferences getInstance(Context context) {
        if (instance == null) {
            instance = new Preferences(context);
        }
        return instance;
    }


    private SharedPreferences sp;

    private Preferences(Context context) {
        sp = context.getSharedPreferences("weex_config", Context.MODE_PRIVATE);
    }

    void putIp(String ip_host) {
        if (ip_host == null) {
            sp.edit().remove("ip:host").apply();
            return;
        }
        sp.edit().putString("ip:host", ip_host).apply();
    }

    String getIp() {
        return sp.getString("ip:host", null);
    }
}
