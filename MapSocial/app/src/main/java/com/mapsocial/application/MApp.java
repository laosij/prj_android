package com.mapsocial.application;

import com.baidu.mapapi.SDKInitializer;
import com.gy.appbase.application.BaseApplication;
import com.gy.utils.log.LogUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.easeui.controller.EaseUI;

/**
 * Created by ganyu on 2016/8/3.
 *
 * super 里头已经有了获取网络。sd卡，等相关工具的方法，直接调用静态方法获取即可
 *
 */
public class MApp extends BaseApplication{

    @Override
    public void onCreate() {
        super.onCreate();

        //百度地图初始化
        SDKInitializer.initialize(this);

        //环信初始化
        EMOptions emOptions = new EMOptions();
        emOptions.setAcceptInvitationAlways(false);
        boolean isOk = EaseUI.getInstance().init(getApplicationContext(), emOptions);
        LogUtils.d("wl", "-----------环信初始化-----------" + isOk);

        //TODO init other things if you want
    }
}
