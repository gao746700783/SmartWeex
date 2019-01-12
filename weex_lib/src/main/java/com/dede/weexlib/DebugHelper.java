package com.dede.weexlib;

import android.annotation.SuppressLint;
import android.content.Context;
import com.dede.weex_public_lib.HotReloadActionListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 用于debug，反射调用了weex_lib_debug module内的一些方法
 */
class DebugHelper {

    private final String SP_KEY_IP_CONFIG = "ip_config";

    @SuppressLint("StaticFieldLeak")
    private static DebugHelper instance;

    static synchronized DebugHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DebugHelper(context.getApplicationContext());
        }
        return instance;
    }

    private Object Config;
    private Method getStringMethod;
    private Method putStringMethod;

    private Constructor<?> reloadConstructor;
    private Method connectMethod;
    private Method destroyMethod;
    private Object hotReloadManager;

    private DebugHelper(Context context) {
        if (!WeexLib.debug) return;
        try {
            Class<?> classPrefer = Class.forName("com.dede.weex_lib_debug.Config");
            Method getInstance = classPrefer.getMethod("getInstance", Context.class);
            Config = getInstance.invoke(null, context);
            getStringMethod = classPrefer.getMethod("getString", String.class);
            putStringMethod = classPrefer.getMethod("putString", String.class, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Class<?> classReload = Class.forName("com.dede.weex_lib_debug.HotReloadManager");
            reloadConstructor = classReload.getConstructor(String.class, HotReloadActionListener.class);
            connectMethod = classReload.getMethod("connect");
            destroyMethod = classReload.getMethod("destroy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化热加载
     *
     * @param ws       socket地址
     * @param listener 刷新回调
     * @return
     */
    private Object createHotReloadManager(String ws, HotReloadActionListener listener) {
        try {
            hotReloadManager = reloadConstructor.newInstance(ws, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hotReloadManager;
    }

    /**
     * 连接热加载
     *
     * @param ws       socket地址
     * @param listener 刷新回调
     */
    void connectHotReload(String ws, HotReloadActionListener listener) {
        if (!WeexLib.debug) return;
        if (connectMethod == null) return;
        if (hotReloadManager == null) {
            hotReloadManager = createHotReloadManager(ws, listener);
        }
        if (hotReloadManager == null) return;
        try {
            connectMethod.invoke(hotReloadManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开热加载连接
     */
    void destroyHotReload() {
        if (!WeexLib.debug) return;
        if (hotReloadManager == null || destroyMethod == null) return;

        try {
            destroyMethod.invoke(hotReloadManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存Ip
     *
     * @param ip_host ip
     */
    void putIp(String ip_host) {
        if (putStringMethod == null) return;
        try {
            putStringMethod.invoke(Config, SP_KEY_IP_CONFIG, ip_host);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Ip
     *
     * @return
     */
    String getIp() {
        if (getStringMethod == null) return null;
        try {
            return (String) getStringMethod.invoke(Config, SP_KEY_IP_CONFIG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
