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
package com.smart.common.weex.adapter;

import android.text.TextUtils;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.adapter.IWXImgLoaderAdapter;
import com.taobao.weex.common.WXImageStrategy;
import com.taobao.weex.dom.WXImageQuality;

public class DefaultImageAdapter implements IWXImgLoaderAdapter {

    public DefaultImageAdapter() {
    }

    @Override
    public void setImage(final String url, final ImageView view,
                         WXImageQuality quality, final WXImageStrategy strategy) {
        if (view == null || view.getLayoutParams() == null) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            view.setImageBitmap(null);
            return;
        }
        if (null != strategy) {
            recordImgLoadAction(strategy.instanceId);
        }

        String temp = url;
        if (url.startsWith("//")) {
            temp = "http:" + url;
        }

        if (url.contains("gif")) {
            Glide.with(WXEnvironment.getApplication())
                    .asGif()
                    .load(temp)
                    .into(view);
        } else {
            Glide.with(WXEnvironment.getApplication())
                    .load(temp)
                    .into(view);
        }
    }

    private void recordImgLoadAction(String instanceId) {
        WXSDKInstance instance = WXSDKManager.getInstance().getAllInstanceMap().get(instanceId);
        if (null == instance || instance.isDestroy()) {
            return;
        }
        instance.getApmForInstance().actionLoadImg();
    }
}
