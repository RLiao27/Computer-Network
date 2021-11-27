package com.me.server.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.me.server.command.ContextThread;
import com.me.server.common.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketService extends Service {

    public ServerSocketService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listen();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(Constants.TAG,"list exception ====>"+e.getMessage());
                }
            }
        }).start();
    }

    public void listen() throws IOException {
        int port = 55555;
        ServerSocket serverSocket = new ServerSocket(port);
        Log.d(Constants.TAG,"ftp server start listen ...");
        while (true) {
            //实际端口是21，避免可能的端口冲突
            Socket socket = serverSocket.accept();
            ContextThread thread = new ContextThread(socket);
            thread.start();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}