package com.me.server;

import android.app.Application;

import com.me.server.command.PublicDataPool;

public class ServerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PublicDataPool.init(this);
    }
}
