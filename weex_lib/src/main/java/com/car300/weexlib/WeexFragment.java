package com.car300.weexlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.common.IWXDebugProxy;
import com.taobao.weex.common.WXRenderStrategy;
import com.taobao.weex.utils.WXFileUtils;

import java.util.HashMap;
import java.util.Map;

import static com.car300.weexlib.WeexLib.registerModule;

/**
 * Created by hsh on 2019/1/9 11:12 AM
 */
public class WeexFragment extends Fragment implements IWXRenderListener {

    private static final String TAG = "WeexFragment";

    private static final String DEFAULT_PAGE_NAME = "weex_page";

    public static final String EXTRA_WEEX_URL = "extra_weex_url";
    public static final String EXTRA_PAGE_NAME = "extra_page_name";

    public static WeexFragment newInstance(String url) {
        WeexFragment weexFragment = new WeexFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_WEEX_URL, url);
        weexFragment.setArguments(bundle);
        return weexFragment;
    }

    public static WeexFragment newInstance(Bundle bundle) {
        WeexFragment weexFragment = new WeexFragment();
        weexFragment.setArguments(bundle);
        return weexFragment;
    }

    private FrameLayout mRootView;
    private WXSDKInstance mWXSDKInstance;
    private HotReloadManager mHotReloadManager;

    private String mUrl = "http://localhost:8081/index.js";
    private Uri mUri;
    private String mPageName = DEFAULT_PAGE_NAME;

    public WXSDKInstance getWXSDKInstance() {
        return mWXSDKInstance;
    }

    private DefaultBroadcastReceiver mBroadcastReceiver;
    private WxReloadListener mReloadListener;
    private WxRefreshListener mRefreshListener;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRootView = new FrameLayout(mContext);

        createWeexInstance();

        Bundle bundle = getArguments();
        if (bundle != null) {
            mPageName = bundle.getString(EXTRA_PAGE_NAME, DEFAULT_PAGE_NAME);
            mUrl = bundle.getString(EXTRA_WEEX_URL, null);
        }

        mUri = Uri.parse(mUrl);

        registerModule();

        if (BuildConfig.DEBUG && !isLocalPage()) {
            remakeUrl();
            registerBroadcastReceiver();
        }

        connectSocket();
        renderPageByURL(mUrl);

        mWXSDKInstance.onActivityCreate();
    }

    private void remakeUrl() {
        String ip = Preferences.getInstance(mContext)
                .getIp();
        if (!TextUtils.isEmpty(ip)) {
            String url = ip + mUri.getPath();
            String query = mUri.getQuery();
            if (query != null) {
                url = url.concat("?").concat(query);
            }
            mUri = Uri.parse(url);
            mUrl = url;
        }
    }

    public void connectSocket() {
        if (!BuildConfig.DEBUG && !isLocalPage()) {
            Log.i(TAG, "Connect Socket: Not debug mode or local page");
            return;
        }
        String ws = "ws://" + mUri.getAuthority();
        Log.i(TAG, "Hot Reload socket: " + ws);

        if (mHotReloadManager != null) {
            mHotReloadManager.connect();
            return;
        }
        mHotReloadManager = new HotReloadManager(ws, new HotReloadManager.ActionListener() {
            @Override
            public void reload() {
                Toast.makeText(mContext, "Reload：" + mUrl, Toast.LENGTH_SHORT).show();
                createWeexInstance();
                renderPage();
            }

            @Override
            public void render(final String bundleUrl) {
                Toast.makeText(mContext, "Render：" + mUrl, Toast.LENGTH_SHORT).show();
                createWeexInstance();
                loadUrl(bundleUrl);
            }
        });
    }

    protected void destoryWeexInstance() {
        if (mWXSDKInstance != null) {
            mWXSDKInstance.registerRenderListener(null);
            mWXSDKInstance.destroy();
            mWXSDKInstance = null;
        }
    }

    protected void createWeexInstance() {
        destoryWeexInstance();
        mWXSDKInstance = new WXSDKInstance(mContext);
        mWXSDKInstance.registerRenderListener(this);
    }

    protected void renderPageByURL(String url) {
        renderPageByURL(url, null);
    }

    protected void renderPageByURL(String url, @Nullable Map<String, Object> options) {
        Log.i(TAG, "RenderPageByURL: " + url);
        if (!isLocalPage()) {
            if (options == null) {
                options = new HashMap<>();
            }
            options.put(WXSDKInstance.BUNDLE_URL, url);
            mWXSDKInstance.renderByUrl(mPageName, url,
                    options, null, WXRenderStrategy.APPEND_ASYNC);
        } else {
            mWXSDKInstance.renderByUrl(mPageName, WXFileUtils.loadAsset(url, mContext),
                    null, null, WXRenderStrategy.APPEND_ASYNC);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView.getParent() != null) {
            ((ViewGroup) mRootView.getParent()).removeView(mRootView);
        }
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerBroadcastReceiver();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityStop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHotReloadManager != null) {
            mHotReloadManager.destroy();
        }
        destoryWeexInstance();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        mRootView.removeAllViews();
        mRootView.addView(view);
    }

    @Override
    public void onRenderSuccess(WXSDKInstance instance, int width, int height) {
        Log.i(TAG, "onRenderSuccess");
    }

    @Override
    public void onRefreshSuccess(WXSDKInstance instance, int width, int height) {
        Log.i(TAG, "onRefreshSuccess");
    }

    @Override
    public void onException(WXSDKInstance instance, String errCode, String msg) {
        Log.e(TAG, "onException ===>>> code: " + errCode + "  msg: " + msg);
    }

    public class DefaultBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IWXDebugProxy.ACTION_DEBUG_INSTANCE_REFRESH.equals(intent.getAction())) {
                if (mRefreshListener != null) mRefreshListener.onRefresh();
            } else if (WXSDKEngine.JS_FRAMEWORK_RELOAD.equals(intent.getAction())) {
                if (mReloadListener != null) mReloadListener.onReload();
            }
        }
    }

    public void registerBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new DefaultBroadcastReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(IWXDebugProxy.ACTION_DEBUG_INSTANCE_REFRESH);
        filter.addAction(WXSDKEngine.JS_FRAMEWORK_RELOAD);
        LocalBroadcastManager.getInstance(mContext.getApplicationContext())
                .registerReceiver(mBroadcastReceiver, filter);
        if (mReloadListener == null) {
            setReloadListener(new WxReloadListener() {
                @Override
                public void onReload() {
                    createWeexInstance();
                    renderPage();
                }
            });
        }

        if (mRefreshListener == null) {
            setRefreshListener(new WxRefreshListener() {
                @Override
                public void onRefresh() {
                    createWeexInstance();
                    renderPage();
                }
            });
        }
    }

    public void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(mContext.getApplicationContext())
                    .unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        setReloadListener(null);
        setRefreshListener(null);
    }


    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void loadUrl(String url) {
        setUrl(url);
        renderPage();
    }

    protected void preRenderPage() {
    }

    protected void postRenderPage() {
    }

    public void reload() {
        remakeUrl();
        createWeexInstance();
        renderPage();
    }

    public void renderPage() {
        preRenderPage();
        renderPageByURL(mUrl);
        postRenderPage();
    }

    protected boolean isLocalPage() {
        boolean isLocalPage = true;
        if (mUri != null) {
            String scheme = mUri.getScheme();
            isLocalPage = !mUri.isHierarchical() ||
                    (!TextUtils.equals(scheme, "http") && !TextUtils.equals(scheme, "https"));
        }
        return isLocalPage;
    }


    public void setRefreshListener(WxRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    public void setReloadListener(WxReloadListener reloadListener) {
        mReloadListener = reloadListener;
    }


    public interface WxReloadListener {
        void onReload();
    }

    public interface WxRefreshListener {
        void onRefresh();
    }
}
