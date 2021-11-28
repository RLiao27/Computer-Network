package com.me.server.command;

import android.util.Log;

import com.me.server.common.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.Socket;

/**
 * RETR命令
 * 用于从服务器获取指定文件到客户端
 * */
public class RetrCommand implements Command{

    @Override
    public void getResult(String data, Writer writer, ContextThread t) {
        Socket s;
        String desDir = t.getNowDir()+File.separator+data;
        File file = new File(desDir);
        Log.d(Constants.TAG,desDir);
        if(file.exists()) {
            try {
                writer.write("150 open ascii mode.\r\n");
                writer.flush();
                s = new Socket(t.getDataIp(), Integer.parseInt(t.getDataPort()));
                BufferedOutputStream dataOut = new BufferedOutputStream(s.getOutputStream());
                byte[] buf = new byte[1024];
                InputStream is = new FileInputStream(file);
                while(-1 != is.read(buf)) {
                    dataOut.write(buf);
                }
                dataOut.flush();
                s.close();
                writer.write("220 transfer complete.\r\n");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                writer.write("221  file not found\r\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
