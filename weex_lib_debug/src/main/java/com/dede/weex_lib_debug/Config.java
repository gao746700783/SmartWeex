package com.dede.weex_lib_debug;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by hsh on 2019/1/9 5:00 PM
 */
class Config {

    private static Config instance;

    public static synchronized Config getInstance(Context context) {
        if (instance == null) {
            instance = new Config(context.getApplicationContext());
        }
        return instance;
    }

    private SharedPreferences sp;

    private Config(Context context) {
        sp = context.getSharedPreferences("weex_debug_config", Context.MODE_PRIVATE);
    }

    /**
     * Save String value to SharedPreference
     *
     * @param key
     * @param value
     */
    public void putString(String key, @Nullable String value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (value == null) {
            sp.edit().remove(key).apply();
        }
        sp.edit().putString(key, value).apply();
    }

    /**
     * Load String value from SharedPreference
     *
     * @param key
     * @return
     */
    @Nullable
    public String getString(String key) {
        return sp.getString(key, null);
    }


    /**
     * Save int number to SharedPreference
     *
     * @param key
     * @param value
     */
    public void putInt(String key, int value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        sp.edit().putInt(key, value).apply();
    }

    /**
     * Load int number from SharedPreference
     *
     * @param key
     * @return
     */
    public int getInt(String key) {
        return sp.getInt(key, 0);
    }
}
