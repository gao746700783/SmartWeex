# weex-lib
A quick integration of Weex's lib to Android


## How to Use

> 添加依赖

    implementation 'com.dede.weexlib:weex_lib:0.0.6'
    debugImplementation 'com.dede.weexlib:weex_lib_debug:0.0.5'// debug mode


> 初始化和使用

    // Application内初始化weex
    WeexLib.with(this)
                .debug(BuildConfig.DEBUG)
                .setImageAdapter { url, view, quality, strategy -> }
                .init()

    // 打开weex页面
    val intent = Intent(this, WeexActivity::class.java)
    intent.putExtra(WeexLib.EXTRA_WEEX_URL, "http://192.168.1.8:8080/index.js")
    startActivity(intent)

> 混淆配置

    -keep class com.taobao.weex.WXDebugTool{*;}
    -keep class com.taobao.weex.devtools.common.LogUtil{*;}
    -keep public class * extends com.taobao.weex.ui.component.WXComponent{*;}
    -keepclassmembers class ** {
      @com.taobao.weex.ui.component.WXComponentProp public *;
    }
    -keep class com.taobao.weex.bridge.**{*;}
    -keep class com.taobao.weex.dom.**{*;}
    -keep class com.taobao.weex.adapter.**{*;}
    -keep class com.taobao.weex.common.**{*;}
    -keep class * implements com.taobao.weex.IWXObject{*;}
    -keep class com.taobao.weex.ui.**{*;}
    -keep class com.taobao.weex.ui.component.**{*;}
    -keep class com.taobao.weex.utils.**{
        public <fields>;
        public <methods>;
        }
    -keep class com.taobao.weex.view.**{*;}
    -keep class com.taobao.weex.module.**{*;}