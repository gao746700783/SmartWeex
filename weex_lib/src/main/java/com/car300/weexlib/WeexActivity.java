package com.car300.weexlib;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.taobao.weex.WXSDKInstance;

/**
 * Created by hsh on 2019/1/7 2:44 PM
 */
public class WeexActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    private final String TAG_WEEX_FRAGMENT = "WEEX_FRAGMENT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeexFragment weexFragment = WeexFragment.newInstance("http://192.168.5.17:8080/index.js");
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, weexFragment, TAG_WEEX_FRAGMENT)
                .commit();
    }

    private WeexFragment getWeexFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_WEEX_FRAGMENT);
        if (fragment == null) return null;
        return (WeexFragment) fragment;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        WeexFragment weexFragment = getWeexFragment();
        if (weexFragment == null) return;
        WXSDKInstance instance = weexFragment.getWXSDKInstance();
        instance.fireGlobalEventCallback("onRestart", null);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!BuildConfig.DEBUG) {
            return super.onKeyUp(keyCode, event);
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showDebugDialog();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        final WeexFragment weexFragment = getWeexFragment();
        if (weexFragment == null) return;
        switch (which) {
            case 0:
                weexFragment.reload();
                break;
            case 1:
                showIpEdit();
                break;
            case 2:
                weexFragment.connectSocket();
                break;
            default:
                break;
        }
    }

    private void showIpEdit() {
        final EditText editText = new EditText(this);
        editText.setHint(R.string.ip_hint);
        String text = Preferences.getInstance(this).getIp();
        if (!TextUtils.isEmpty(text)) editText.setText(text);
        new AlertDialog.Builder(this)
                .setTitle(R.string.ip_dialog_title)
                .setView(editText)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String string = editText.getText().toString();
                        if (TextUtils.isEmpty(string)) {
                            Preferences.getInstance(WeexActivity.this)
                                    .putIp(null);
                            return;
                        }
                        Uri uri = Uri.parse(string);
                        if (!uri.isHierarchical()) {
                            Toast.makeText(WeexActivity.this, R.string.toast_ip_error, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        string = uri.getScheme() + "://" + uri.getAuthority();
                        Preferences.getInstance(WeexActivity.this)
                                .putIp(string);
                    }
                })
                .create()
                .show();
    }

    private void showDebugDialog() {
        new AlertDialog.Builder(this)
                .setItems(R.array.debug_dialog_item, this)
                .create()
                .show();
    }
}