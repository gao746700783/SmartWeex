package com.dede.weex_lib_debug;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.dede.weex_public_lib.HotReloadActionListener;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by hsh on 2019/1/9 11:12 AM
 */
class HotReloadManager {

    private static final String TAG = "HotReloadManager";

    private HotReloadActionListener listener;
    private String ws;

    private WebSocket session;
    private OkHttpClient client = new OkHttpClient.Builder()
            .build();

    private Handler handler = new Handler(Looper.getMainLooper());

    private void uiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public HotReloadManager(String ws, final HotReloadActionListener actionListener) {
        if (TextUtils.isEmpty(ws) || actionListener == null) {
            Log.w(TAG, "Illegal arguments");
            return;
        }
        this.listener = actionListener;
        this.ws = ws;

        connect();
    }

    public void connect() {
        destroy();

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
                            uiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listener.reload();
                                }
                            });
                        } else if ("WXReloadBundle".equals(method)) {
                            final String bundleUrl = rpcMessage.optString("params", null);
                            uiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listener.render(bundleUrl);
                                }
                            });
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

    public void destroy() {
        if (session == null) return;
        session.close(1001, "GOING_AWAY");
        session = null;
    }
}
