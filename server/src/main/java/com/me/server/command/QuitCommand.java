package com.me.server.command;

import java.io.IOException;
import java.io.Writer;
/**
 * QUIT命令:
 * 客户端注销ftp服务器
 * */
public class QuitCommand implements Command {

    @Override
    public void getResult(String data, Writer writer, ContextThread t) {
        try {
            writer.write("221 goodbye.\r\n");
            writer.flush();
            writer.close();
            t.getSocket().close();
            t.setCount(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}  