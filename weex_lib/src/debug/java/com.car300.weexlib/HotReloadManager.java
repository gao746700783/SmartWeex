package com.car300.weexlib;

import android.text.TextUtils;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.utils.WXLogUtils;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hsh on 2019/1/9 11:12 AM
 */
class HotReloadManager {

    private static final String TAG = "HotReloadManager";

    private ActionListener listener;
    private String ws;

    private WebSocket session;
    private OkHttpClient client = new OkHttpClient.Builder()
            .build();

    private void uiThread(Runnable runnable) {
        WXSDKManager.getInstance().postOnUiThread(runnable, 0);
    }

    HotReloadManager(String ws, final ActionListener actionListener) {
        if (TextUtils.isEmpty(ws) || actionListener == null) {
            WXLogUtils.w(TAG, "Illegal arguments");
            return;
        }
        this.listener = actionListener;
        this.ws = ws;

        connect();
    }

    void connect() {
        destroy();

        Request request = new Request.Builder()
                .url(ws)
                .build();
        session = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                WXLogUtils.d(TAG, "ws session open");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                WXLogUtils.d(TAG, "on message: " + text);
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
                WXLogUtils.d(TAG, "Closed:" + code + ", " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                t.printStackTrace();
            }
        });
    }

    void destroy() {
        if (session == null) return;
        session.close(1001, "GOING_AWAY");
        session = null;
    }

    public interface ActionListener {
        void reload();

        void render(String bundleUrl);
    }
}
