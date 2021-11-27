package com.me.server.command;

import android.util.Log;

import com.me.server.common.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

/**
 * 线程
 * */
public class ContextThread extends Thread{
    //counter
    private int mCount = 0;
    //用于与客户端通信的 tcp socket
    private Socket mSocket;
    //数据通道的IP地址
    private String mDataIp;
    //数据通道端口
    private  String mDataPort;
    //登录
    private boolean isLogin = false;
    //当前操作目录
    private String mNowDir = PublicDataPool.sRootDir;
    //当前模式
    private String mMode="control";

    public static final ThreadLocal<String> sUser = new ThreadLocal<String>();

    public String getNowDir() {
        return mNowDir;
    }
    public void setNowDir(String nowDir) {
        this.mNowDir = nowDir;
    }

    public void setIsLogin(boolean t) {
        isLogin = t;
    }

    public boolean getIsLogin() {
        return isLogin;
    }

    public Socket getSocket() {
        return mSocket;
    }

    public String getDataIp() {
        return mDataIp;
    }

    public void setDataIp(String dataIp) {
        this.mDataIp = dataIp;
    }

    public String getDataPort() {
        return mDataPort;
    }

    public void setDataPort(String dataPort) {
        this.mDataPort = dataPort;
    }

    public void setCount(int count) { this.mCount = count; }

    public ContextThread(Socket socket) {
        this.mSocket = socket;
    }

    public ContextThread(Socket socket, String mode)
    {
        this.mSocket=socket;
        this.mMode=mode;
    }

    public void run() {
        Log.d(Constants.TAG,"a new client is connected= ");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            Writer writer = new OutputStreamWriter(mSocket.getOutputStream());
            while(true) {
                if(mCount == 0 && this.mMode.equals("control")) {
                    writer.write("220 welcome my ftp server, Server ready.\r\n");
                    writer.flush();
                    mCount++;
                } else {
                    if(!mSocket.isClosed()) {
                        String command = reader.readLine();
                        Log.d(Constants.TAG,command);
                        if(command !=null) {
                            //parse command from client
                            String[] datas = command.split(" ");
                            //create command object
                            Command commandSolver = CommandFactory.createCommand(datas[0]);
                            if(commandSolver != null) { //check the command first
                                if (CheckNoNeedLogin(commandSolver)) {
                                    if (commandSolver == null) {
                                        writer.write("502  invalid command,try again");
                                    } else {
                                        String data = "";
                                        if (datas.length >= 2) {
                                            data = datas[1];
                                        }
                                        //set data for command
                                        commandSolver.getResult(data, writer, this);
                                    }
                                } else {
                                    writer.write("532 need login first，please login and then try again\r\n");
                                    writer.flush();
                                }
                            }else{
                                writer.write("598 invalid command, please check \r\n");
                                writer.flush();
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.d(Constants.TAG,"finish tcp connection");
        }
    }

   /**
     * 检查是否需要登录才能处理命令
     * @param command 客户指令
     * @return true: 登录  false: 需要登录
     * */
    public boolean CheckNoNeedLogin(Command command) {
        if(command instanceof UserCommand
                || command instanceof PassCommand) {
            return true;
        } else {
            return isLogin;
        }
    }

}  