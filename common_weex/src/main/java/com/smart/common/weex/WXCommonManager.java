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
package com.smart.common.weex;

import android.app.Application;
import android.content.Context;
import com.smart.common.weex.adapter.ImageAdapter;
import com.taobao.weex.InitConfig;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.adapter.IWXImgLoaderAdapter;
import com.taobao.weex.common.WXException;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.ui.component.WXComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * WXCommonManager
 *
 * 单例 ，控制weex js configs等
 */
public final class WXCommonManager {

    private static final String TAG = "WXCommonManager";

    private static WXCommonManager wxCommonManager;

    private String mWeexPath;             // weex js 保存路径
    private String mWeexConfigs;          // weex config 配置，一般是服务端下发

    private HashMap<String, Class<? extends WXModule>> mModules = new HashMap<>();
    private HashMap<String, Class<? extends WXComponent>> mComponents = new HashMap<>();

    public static WXCommonManager getInstance(){
        if (wxCommonManager == null){
            synchronized (WXCommonManager.class) {
                if (wxCommonManager == null) {
                    wxCommonManager = new WXCommonManager();
                }
            }
        }
        return wxCommonManager;
    }

    public void initWeex(Context context) {
        initWeex(context,new ImageAdapter());
    }

    public void initWeex(Context context, IWXImgLoaderAdapter iwxImgLoaderAdapter) {
        if (!(context instanceof Application)){
            throw new IllegalArgumentException("Context must be application," +
                    "please call init in application's onCreate");
        }

        Application application = (Application) context;
        WXSDKEngine.initialize(application, new InitConfig.Builder().setImgAdapter(iwxImgLoaderAdapter).build());

        //
        // Context mContext = context.getApplicationContext();
        mWeexPath = context.getFilesDir().getPath();
    }

    public void registerModule(Class<? extends WXModule> module, String module_name) {
        if (null == module) return;
        if (!mModules.containsKey(module_name)) {
            mModules.put(module_name, module);
            try {
                WXSDKEngine.registerModule(module_name, module);
            } catch (WXException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerModule(HashMap<String, Class<? extends WXModule>> modules) {
        if (modules.isEmpty()) return;

        Set<Map.Entry<String, Class<? extends WXModule>>> entrySet = modules.entrySet();
        for (Map.Entry<String, Class<? extends WXModule>> entry : entrySet) {
            registerModule(entry.getValue(),entry.getKey());
        }
    }

    public void registerComponent(Class<? extends WXComponent> component, String component_name) {
        if (null == component) return;
        if (!mComponents.containsKey(component_name)) {
            mComponents.put(component_name, component);
            try {
                WXSDKEngine.registerComponent(component_name, component);
            } catch (WXException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerComponent(HashMap<String, Class<? extends WXComponent>> components) {
        if (components.isEmpty()) return;

        Set<Map.Entry<String, Class<? extends WXComponent>>> entrySet = components.entrySet();
        for (Map.Entry<String, Class<? extends WXComponent>> entry : entrySet) {
            registerComponent(entry.getValue(),entry.getKey());
        }
    }

    public String getWeexPath() {
        return mWeexPath;
    }

    public String getWeexConfigs() {
        return mWeexConfigs;
    }

    public void setWeexConfigs(String weexConfigs) {
        this.mWeexConfigs = weexConfigs;
    }

}
