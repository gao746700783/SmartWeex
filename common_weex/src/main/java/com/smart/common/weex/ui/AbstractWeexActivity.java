/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.smart.common.weex.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.alibaba.fastjson.JSONObject;
import com.smart.common.weex.WXCommonManager;
import com.smart.common.weex.util.AssertUtil;
import com.smart.common.weex.util.CommonConstants;
import com.smart.common.weex.util.hotreload.HotRefreshManager;
import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.common.Constants;
import com.taobao.weex.common.WXRenderStrategy;
import com.taobao.weex.utils.WXUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sospartan on 5/30/16.
 */
public abstract class AbstractWeexActivity extends AppCompatActivity implements IWXRenderListener, Handler.Callback {
    protected static final String TAG = "AbstractWeexActivity";

    protected ViewGroup mContainer;
    protected WXSDKInstance mInstance;

    protected WXAnalyzerDelegate mWxAnalyzerDelegate;

    //    protected String mUrl;
//    private String mWsUrl;
    private Uri mUri;
    private Handler mWXHandler;
    private RefreshBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createWeexInstance();
        mInstance.onActivityCreate();

        mWxAnalyzerDelegate = new WXAnalyzerDelegate(this);
        mWxAnalyzerDelegate.onCreate();
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        // debug 模式 可使用HotReload特性
        if (WXCommonManager.getInstance().isDebug()) {
            mWXHandler = new Handler(this);
            HotRefreshManager.getInstance().setHandler(mWXHandler);

            registerBroadcastReceiver();
        }

    }

    protected void createWeexInstance() {
        destroyWeexInstance();

        Rect outRect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);

        mInstance = new WXSDKInstance(this);
        mInstance.registerRenderListener(this);
    }

    protected void destroyWeexInstance() {
        if (mInstance != null) {
            mInstance.registerRenderListener(null);
            mInstance.destroy();
            mInstance = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mInstance != null) {
            mInstance.onActivityStart();
        }
        if (mWxAnalyzerDelegate != null) {
            mWxAnalyzerDelegate.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mInstance != null) {
            mInstance.onActivityResume();
        }
        if (mWxAnalyzerDelegate != null) {
            mWxAnalyzerDelegate.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mInstance != null) {
            mInstance.onActivityPause();
        }
        if (mWxAnalyzerDelegate != null) {
            mWxAnalyzerDelegate.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mInstance != null) {
            mInstance.onActivityStop();
        }
        if (mWxAnalyzerDelegate != null) {
            mWxAnalyzerDelegate.onStop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mInstance != null) {
            mInstance.onActivityDestroy();
        }
        if (mWxAnalyzerDelegate != null) {
            mWxAnalyzerDelegate.onDestroy();
        }

        unregisterBroadcastReceiver();
    }

    @Override
    public void onViewCreated(WXSDKInstance wxsdkInstance, View view) {
        View wrappedView = null;
        if (mWxAnalyzerDelegate != null) {
            wrappedView = mWxAnalyzerDelegate.onWeexViewCreated(wxsdkInstance, view);
        }
        if (wrappedView != null) {
            view = wrappedView;
        }
        if (mContainer != null) {
            mContainer.removeAllViews();
            mContainer.addView(view);
        }
    }


    @Override
    public void onRefreshSuccess(WXSDKInstance wxsdkInstance, int i, int i1) {

    }

    @Override
    @CallSuper
    public void onRenderSuccess(WXSDKInstance instance, int width, int height) {
        if (mWxAnalyzerDelegate != null) {
            mWxAnalyzerDelegate.onWeexRenderSuccess(instance);
        }
    }

    @Override
    @CallSuper
    public void onException(WXSDKInstance instance, String errCode, String msg) {
        if (mWxAnalyzerDelegate != null) {
            mWxAnalyzerDelegate.onException(instance, errCode, msg);
        }
    }

    @Override
    @CallSuper
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return (mWxAnalyzerDelegate != null && mWxAnalyzerDelegate.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mInstance != null) {
            mInstance.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected final void setContainer(ViewGroup container) {
        mContainer = container;
    }

    // <!-------------------- renderPage ---------------------->

    protected String getPageName() {
        return TAG;
    }

    protected void renderPage(String url, String template) {
        renderPage(url, template, null);
    }

    protected void renderPage(String url, String template, String jsonInitData) {
        AssertUtil.throwIfNull(mContainer, new RuntimeException("Can't render page, container is null"));

        Map<String, Object> options = new HashMap<>();
        options.put(WXSDKInstance.BUNDLE_URL, url);
        // Set options.bundleDigest
        try {
            String banner = WXUtils.getBundleBanner(template);
            JSONObject jsonObj = JSONObject.parseObject(banner);
            String digest = null;
            if (jsonObj != null) {
                digest = jsonObj.getString(Constants.CodeCache.BANNER_DIGEST);
            }
            if (digest != null) {
                options.put(Constants.CodeCache.DIGEST, digest);
            }
        } catch (Throwable t) {
        }

        //Set options.codeCachePath
        String path = WXEnvironment.getFilesDir(getApplicationContext());
        path += File.separator;
        path += Constants.CodeCache.SAVE_PATH;
        path += File.separator;
        options.put(Constants.CodeCache.PATH, path);

        mInstance.setTrackComponent(true);
        mInstance.render(
                getPageName(),
                template,
                options,
                jsonInitData,
                WXRenderStrategy.APPEND_ASYNC);
    }

    protected void renderPageByURL(String url) {
        renderPageByURL(url, null);
    }

    protected void renderPageByURL(String url, String jsonInitData) {
        AssertUtil.throwIfNull(mContainer, new RuntimeException("Can't render page, container is null"));

        Map<String, Object> options = new HashMap<>();
        options.put(WXSDKInstance.BUNDLE_URL, url);

        mInstance.setTrackComponent(true);
        mInstance.renderByUrl(
                getPageName(),
                url,
                options,
                jsonInitData,
                WXRenderStrategy.APPEND_ASYNC);
    }

    protected void loadWxPage(String url, String bundleUrl, String jsonInitData) {
//        this.setUrl(url);

        mUri = Uri.parse(url);

        // find local js
        boolean isLocalPageExist = findWxPage(url);

        if (isLocalPageExist) {
            startHotRefresh();
            renderPage(url, bundleUrl, jsonInitData);
        } else {
            startHotRefresh();
            renderPageByURL(url, jsonInitData);
        }
    }

    private boolean findWxPage(String url) {
        return false;
    }

    // <!-------------------- hotReload ---------------------->

    private void registerBroadcastReceiver() {
        mReceiver = new RefreshBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WXSDKInstance.ACTION_DEBUG_INSTANCE_REFRESH);
        filter.addAction(WXSDKInstance.ACTION_INSTANCE_RELOAD);

        registerReceiver(mReceiver, filter);
    }

    private void unregisterBroadcastReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        mReceiver = null;
    }

    public class RefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WXSDKInstance.ACTION_INSTANCE_RELOAD.equals(intent.getAction()) ||
                    WXSDKInstance.ACTION_DEBUG_INSTANCE_REFRESH.equals(intent.getAction())) {
                String myUrl = intent.getStringExtra("url");
                Log.e("WXPageActivity", "RefreshBroadcastReceiver reload onReceive ACTION_DEBUG_INSTANCE_REFRESH mBundleUrl:" + myUrl + " mUri:" + mUri);

                startHotRefresh();
                loadWxPage(myUrl, null, null);
            }
        }
    }

    /**
     * hot refresh
     */
    private void startHotRefresh() {
        try {
            mUri.getAuthority();
            URL url = new URL(mUri.toString());
            String host = url.getHost();
            String query = url.getQuery();
//            int port = url.getPort();
            String port = TextUtils.isEmpty(query) ? "8082" : getUrlParam("port", query);
            String wsUrl = "ws://" + host + ":" + port;
            mWXHandler.obtainMessage(CommonConstants.HOT_REFRESH_CONNECT, 0, 0, wsUrl).sendToTarget();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private String getUrlParam(String key, String queryString) {
        String regex = "[?|&]?" + key + "=([^&]+)";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(queryString);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.d(TAG,"handleMessage called:"+msg.what);
        switch (msg.what) {
            case CommonConstants.HOT_REFRESH_CONNECT:
                String wsUrl = msg.obj.toString();
                Log.d(TAG, wsUrl);
                HotRefreshManager.getInstance().connect(wsUrl);
                break;
            case CommonConstants.HOT_REFRESH_DISCONNECT:
                HotRefreshManager.getInstance().disConnect();
                break;
            case CommonConstants.HOT_REFRESH_REFRESH:
                createWeexInstance();
                loadWxPage(mUri.toString(), null, null);
                break;
            case CommonConstants.HOT_REFRESH_CONNECT_ERROR:
                Toast.makeText(this, "hot refresh connect error!", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        return false;
    }


}
