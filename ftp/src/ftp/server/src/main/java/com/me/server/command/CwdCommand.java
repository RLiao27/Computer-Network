package com.me.server.command;

import android.text.TextUtils;
import android.util.Log;

import com.me.server.common.Constants;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * CMD Command:
 * 用于切换服务器的运行目录
 * */
public class CwdCommand implements Command{

    @Override
    public void getResult(String data, Writer writer, ContextThread t) {

        if(TextUtils.isEmpty(data)){ // 数据为空，则设置当前目录为root
            t.setNowDir(PublicDataPool.sRootDir);
            Log.d(Constants.TAG, "nowDri =====>" + PublicDataPool.sRootDir);
            //显示结果
            try {
                writer.write("250 CWD command successfully");
                writer.write("\r\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            //获取操作目录
            String dir = t.getNowDir() + File.separator + data;
            File file = new File(dir);
            try {
                //检查目录是否可用
                if ((file.exists()) && (file.isDirectory())) {
                    String nowDir = t.getNowDir() + File.separator + data;
                    t.setNowDir(nowDir);
                    Log.d(Constants.TAG, "nowDri =====>" + nowDir);
                    //显示结果
                    writer.write("250 CWD command successfully");
                } else {
                    writer.write("550 not found folder");
                }
                writer.write("\r\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
