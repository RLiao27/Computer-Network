package com.me.server.command;

import java.io.IOException;
import java.io.Writer;

/**
 * USER命令
 * */
public class UserCommand implements Command {

    @Override
    public void getResult(String data, Writer writer, ContextThread t) {
        String response = "";
        if(PublicDataPool.sUsers.containsKey(data)) {
            ContextThread.sUser.set(data);
            response = "331 please input your password";
        } else {
            response = "501 user is not validate";
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
