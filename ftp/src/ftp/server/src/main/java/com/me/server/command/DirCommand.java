package com.me.server.command;

import android.text.TextUtils;
import android.util.Log;

import com.me.server.common.Constants;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;
/**
 * Dir 命令:
 * 用于查看服务器指定目录下的文件
 * */
public class DirCommand implements Command{

    @Override
    public void getResult(String data, Writer writer, ContextThread t) {
        Log.d(Constants.TAG,"execute LIST command........");
        String desDir = t.getNowDir();
        if(!TextUtils.isEmpty(data)){
            desDir = desDir+File.separator+data;// 指定目录
        }
        Log.d(Constants.TAG,desDir);
        File dir = new File(desDir);
        if(!dir.exists()) {
            try {
                writer.write("210  folder not found\r\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(Constants.TAG,"files of folder:");
            Vector<String> allFiles=new Vector<>();
            String[] lists= dir.list();
            String flag = null;
            for(String name : lists) {
                File temp = new File(desDir+File.separator+name);
                if(temp.isDirectory()) {
                    flag = "d";
                } else {
                    flag = "f";
                }
                String oneinfo=flag+"rw-rw-rw-   1 ftp      ftp            "+temp.length()+" Dec 30 17:07 "+name;
                Log.d(Constants.TAG,oneinfo);
                allFiles.add(oneinfo);
            }

            try {
                writer.write("150 Opening data connection for directory list...\r\n");
                writer.flush();
                for(String item : allFiles) {
                    writer.write(item);
                    writer.write("\r\n");
                    writer.flush();
                }
                writer.write("end of files\r\n");
                writer.flush();
                writer.write("226 transfer complete...\r\n");
                writer.flush();
                Log.d(Constants.TAG,"*********************************************");
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
            }
        }

    }

}  