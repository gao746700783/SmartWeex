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

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import com.taobao.weex.WXSDKInstance;

import java.util.List;

/**
 * Basic Weex powered Activity.
 * Created by sospartan on 5/31/16.
 */
public class WeexPageActivity extends AbstractWeexActivity {

    public static final String EXTRA_URL = "extra_url";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainer((ViewGroup) findViewById(android.R.id.content));

        Uri uri = getIntent().getData();
        if (uri != null) {
            // 完整的url信息
            String url = uri.toString();
            Log.e(TAG, "url: " + url);
            // scheme部分
            String scheme = uri.getScheme();
            Log.e(TAG, "scheme: " + scheme);
            // host部分
            String host = uri.getHost();
            Log.e(TAG, "host: " + host);
            //port部分
            int port = uri.getPort();
            Log.e(TAG, "port: " + port);
            // 访问路劲
            String path = uri.getPath();
            Log.e(TAG, "path: " + path);
            List<String> pathSegments = uri.getPathSegments();
            // Query部分
            String query = uri.getQuery();
            Log.e(TAG, "query: " + query);
            //获取指定参数值
            String to = uri.getQueryParameter("to");
            Log.e(TAG, "jump to url: " + to);

            renderWxPage(to, null, null);
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(EXTRA_URL)) {
                String url = extras.getString(EXTRA_URL);

                renderWxPage(url, null, null);
            }
        }

    }

    @Override
    public void onRenderSuccess(WXSDKInstance instance, int width, int height) {
        super.onRenderSuccess(instance, width, height);
    }

    @Override
    public void onException(WXSDKInstance instance, String errCode, String msg) {
        super.onException(instance, errCode, msg);
    }

}
