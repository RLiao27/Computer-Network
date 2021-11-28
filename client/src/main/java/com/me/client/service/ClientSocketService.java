package com.me.client.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ClientSocketService extends Service {

    public ClientSocketService() { }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


}