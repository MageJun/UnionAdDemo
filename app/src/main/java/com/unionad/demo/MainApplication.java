package com.unionad.demo;

import android.app.Application;

import com.unionad.sdk.ad.AdClient;



/**
 * ----------注意事项----------
 *
 * 1、我们所有的请求已经做过线程优化处理，不需要单独放到线程中去执行，内部是非阻塞实现
 *
 * 2、初始化只能在主进程中初始化，暂时不支持多进程
 *
 * 请在SDK集成之后，给到我们一个回测包，我们会帮忙测试广告数据和接口处理是否正确
 *
 *
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AdClient.init(getApplicationContext());
    }
}
