# weex-lib
A quick integration of Weex's lib to Android


## How to Use

> Edit build.gradle

    implementation 'com.dede.weexlib:weex_lib:0.0.6'
    debugImplementation 'com.dede.weexlib:weex_lib_debug:0.0.5'// debug mode


> Java Code

    // Init Application
    WeexLib.with(this)
                .debug(BuildConfig.DEBUG)
                .setImageAdapter { url, view, quality, strategy -> }
                .init()

    // Open Weex page
    val intent = Intent(this, WeexActivity::class.java)
    intent.putExtra(WeexLib.EXTRA_WEEX_URL, "http://192.168.1.8:8080/index.js")
    startActivity(intent)