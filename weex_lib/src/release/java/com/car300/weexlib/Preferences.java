package com.car300.weexlib;

import android.content.Context;

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


    private Preferences(Context context) {
    }

    void putIp(String ip_host) {
    }

    String getIp() {
        return null;
    }
}
