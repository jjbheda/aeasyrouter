package com.routercore.callback;

import com.routercore.Postcard;

public interface NavigationCallback {

    //找到跳转页面
    void onFound(Postcard postcard);

    //未找到
    void onLost(Postcard postcard);

    //成功跳转
    void onArrival(Postcard postcard);
}
