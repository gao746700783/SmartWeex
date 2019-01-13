package com.dede.weexlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

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
    private Method hotReloadHandlerMethod;
    private Handler hotReloadHandler;

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
            reloadConstructor = classReload.getConstructor(Handler.class);
            reloadConstructor.setAccessible(true);
            hotReloadHandlerMethod = classReload.getMethod("getHotReloadHandler");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化热加载
     *
     * @param handler 刷新回调
     * @return
     */
    private void initHotReloadManager(Handler handler) {
        if (reloadConstructor == null || hotReloadHandlerMethod == null) return;
        try {
            Object hotReloadManager = reloadConstructor.newInstance(handler);
            hotReloadHandler = (Handler) hotReloadHandlerMethod.invoke(hotReloadManager);// 获取handler
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接热加载
     *
     * @param ws      socket地址
     * @param handler 刷新回调
     */
    void connectHotReload(String ws, Handler handler) {
        if (!WeexLib.debug) return;
        if (hotReloadHandler == null) {
            initHotReloadManager(handler);
        }
        if (hotReloadHandler == null) return;
        hotReloadHandler.sendMessage(Message.obtain(hotReloadHandler, 1, ws));
    }

    /**
     * 断开热加载连接
     */
    void destroyHotReload() {
        if (!WeexLib.debug) return;
        if (hotReloadHandler == null) return;
        hotReloadHandler.sendMessage(Message.obtain(hotReloadHandler, -1));
        hotReloadHandler = null;
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
