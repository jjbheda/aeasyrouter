package com.arouter;

import android.app.Application;

import com.routercore.AEasyRouter;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AEasyRouter.init(this);
    }
}
