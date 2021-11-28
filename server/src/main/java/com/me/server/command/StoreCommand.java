package com.me.server.command;

import android.text.TextUtils;
import android.util.Log;

import com.me.server.common.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.net.Socket;

/**
 * STOR命令
 * 用于上传文件到服务器上的指定目录
 * */
public class StoreCommand implements Command{

    @Override
    public void getResult(String data, Writer writer, ContextThread t) {
        Log.d(Constants.TAG,"execute store command");
        try{
            writer.write("150 Binary data connection\r\n");
            writer.flush();
            //为给定路径创建文件夹
            if(data.endsWith(":d")){
                String[] params = data.split(":");
                if(!TextUtils.isEmpty(params[0])){
                    Log.d(Constants.TAG,"store folder: "+params[0]);
                    File folder = new File(t.getNowDir() + "/" + params[0]);
                    if(!folder.exists()){
                        folder.mkdirs();
                    }
                }
            }else {
               //根据给定的路径创建文件
                RandomAccessFile inFile = new RandomAccessFile(t.getNowDir() + "/" + data, "rw");
                Log.d(Constants.TAG,"store file: "+t.getNowDir() + "/" + data);
                Socket tempSocket = new Socket(t.getDataIp(), Integer.parseInt(t.getDataPort()));
                InputStream inSocket = tempSocket.getInputStream();
                byte[] buffer = new byte[1024];
                int amount;
                while ((amount = inSocket.read(buffer)) != -1) {
                    inFile.write(buffer, 0, amount);
                }
                Log.d(Constants.TAG, "transmit over,close connection.");
                inFile.close();
                inSocket.close();
                tempSocket.close();
            }
            writer.write("226 transfer complete\r\n");
            writer.flush();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}  