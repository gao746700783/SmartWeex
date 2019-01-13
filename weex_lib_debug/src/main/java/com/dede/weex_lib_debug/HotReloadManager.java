package com.dede.weex_lib_debug;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by hsh on 2019/1/9 11:12 AM
 */
class HotReloadManager {

    private static final String TAG = "HotReloadManager";

    private Handler handler;

    private WebSocket session;
    private OkHttpClient client = new OkHttpClient.Builder()
            .build();

    public HotReloadManager(Handler handler) {
        if (handler == null) {
            Log.w(TAG, "Illegal arguments");
            return;
        }
        this.handler = handler;
    }

    @SuppressLint("HandlerLeak")
    private Handler hotReloadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String ws = (String) msg.obj;
                    connect(ws);
                    break;
                case -1:
                    destroy();
                    break;
            }
        }
    };

    public Handler getHotReloadHandler() {
        return hotReloadHandler;
    }

    private void connect(String ws) {
        close();

        Request request = new Request.Builder()
                .url(ws)
                .build();
        session = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "ws session open");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "on message: " + text);
                try {
                    JSONObject rpcMessage = new JSONObject(text);
                    String method = rpcMessage.optString("method", null);
                    if (!TextUtils.isEmpty(method)) {
                        if ("WXReload".equals(method)) {
                            handler.sendEmptyMessage(1);
                        } else if ("WXReloadBundle".equals(method)) {
                            String bundleUrl = rpcMessage.optString("params", null);
                            handler.sendMessage(Message.obtain(handler, 2, bundleUrl));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Closed:" + code + ", " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                t.printStackTrace();
            }
        });
    }

    private void close() {
        if (session == null) return;
        session.close(1001, "GOING_AWAY");
        session = null;
    }

    private void destroy() {
        close();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (hotReloadHandler != null) {
            hotReloadHandler.removeCallbacksAndMessages(null);
            hotReloadHandler = null;
        }
    }
}
