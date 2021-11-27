package com.me.server.command;

import java.io.Writer;
/**
 * FTP 命令接口
 * */
interface Command {

    /**
     * @param data    接收除cmd外的数据
     * @param writer  socket 输出流
     * @param t       线程
     * */
    void getResult(String data, Writer writer, ContextThread t);

}  