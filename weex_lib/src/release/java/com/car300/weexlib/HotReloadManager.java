package com.car300.weexlib;

/**
 * Created by hsh on 2019/1/9 11:12 AM
 */
class HotReloadManager {

    HotReloadManager(String ws, final ActionListener actionListener) {
    }

    void connect() {
    }

    void destroy() {
    }

    public interface ActionListener {
        void reload();

        void render(String bundleUrl);
    }
}
