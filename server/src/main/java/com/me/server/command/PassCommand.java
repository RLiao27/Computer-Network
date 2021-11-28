package com.me.server.command;

import android.util.Log;

import com.me.server.common.Constants;

import java.io.IOException;
import java.io.Writer;

public class PassCommand implements Command{

    @Override
    public void getResult(String data, Writer writer, ContextThread t) {
        Log.d(Constants.TAG,"execute the pass command");
        Log.d(Constants.TAG,"the data is "+data);
        String key = ContextThread.sUser.get();
        String response = null;
        if("anonymous".equals(key)){ //匿名用户，不需要检查密码
            Log.d(Constants.TAG, "login successfully ");
            PublicDataPool.sLoginUser.add(key);
            t.setIsLogin(true);
            response = "230 User " + key + " logged in";
        }else { //普通用户
            String pass = PublicDataPool.sUsers.get(key);
            if (pass.equals(data)) {
                Log.d(Constants.TAG, "login successfully ");
                PublicDataPool.sLoginUser.add(key);
                t.setIsLogin(true);
                response = "230 User " + key + " logged in";
            } else {
                Log.d(Constants.TAG, "login failed,wrong password ");
                response = "530   wrong password";
            }
        }
        try {
            writer.write(response);
            writer.write("\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}  