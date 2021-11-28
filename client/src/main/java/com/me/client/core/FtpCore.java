package com.me.client.core;

import android.util.Log;

import com.me.client.ClientApplication;
import com.me.client.common.Constants;
import com.me.client.utils.NetworkUtils;

import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * FTP函数类
 * */
public class FtpCore {

    //实际端口为21，这里是为了避免可能的端口冲突
    private static final int PORT = 55555;

    public static boolean bLogin;
    private BufferedReader mControlReader;
    private PrintWriter mControlOut;
    private String mUserName;
    private String mPassword;
    private static FtpCore sInstance;
    private Socket mSocket;
    private String mHostIp;

    public static FtpCore instance() {
        if (sInstance == null) {
            sInstance = new FtpCore();
        }
        return sInstance;
    }

    private FtpCore() { }

    /**
     * 登录ftp服务器
     * @param ip ftp服务器ip
     * @param username 登录用户的用户名
     * @param password 登录用户密码
     * */
    public String login(String ip, String username, String password) {
        String result = "";
        try {
            mSocket = new Socket(ip, PORT);
            mHostIp = ip;
            mUserName = username;
            mPassword = password;
            mControlReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mControlOut = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()), true);
            result = initFtp();
        } catch (Exception e) {
            e.printStackTrace();
            result = "1000";
        }
        return result;
    }

     /**
     * 初始化ftp
     * */
    public String initFtp() throws Exception {
        String msg;
        do {
            msg = mControlReader.readLine();
            Log.d(Constants.TAG, msg);
        } while (!msg.startsWith("220 "));
        mControlOut.println("USER " + mUserName);
        String response = mControlReader.readLine();
        Log.d(Constants.TAG, response);
        if (response.startsWith("501")) {
            return "501";
        } else if (response.startsWith("331")) {
            mControlOut.println("PASS " + mPassword);
            response = mControlReader.readLine();
            Log.d(Constants.TAG, response);
            if (response.startsWith("530 ")) {
                return "530";
            } else if (response.startsWith("230")) {
                bLogin = true;
                return "";
            }
        }
        return "500";
    }

     /**
     * 获取当前目录下的所有文件，取决于服务器端当前文件目录
     * */
    public FTPFile[] getAllFile() throws Exception {
        String response;
        // 发送 LIST 命令
        mControlOut.println("LIST");
        // 读取命令响应
        response = mControlReader.readLine();
        Log.d(Constants.TAG, response);

        // 从服务器读取数据
        Vector<FTPFile> tempfiles = new Vector<>();

        String line = null;
        while ((line = mControlReader.readLine()) != null) {
            if (line.equals("end of files"))
                break;
            Log.d(Constants.TAG, line);
            FTPFile temp = new FTPFile();
            setFtpFileInfo(temp, line);
            tempfiles.add(temp);
        }
        // 读取命令响应
        response = mControlReader.readLine();
        Log.d(Constants.TAG, response);
        FTPFile[] files = new FTPFile[tempfiles.size()];
        tempfiles.copyInto(files);
        return files;
    }

    /**
     * 获取指定目录下的所有文件
     * @param  name 如果name为空，将获取根目录下的所有文件
     * ，否则将获取指定目录中的所有文件
     * */
    public FTPFile[] getFolderFiles(String name) throws Exception
    {
        String response;
        // 发送 LIST 命令
        mControlOut.println("LIST "+name);
        // 读取命令响应
        response = mControlReader.readLine();
        Log.d(Constants.TAG, response);

        // 从服务器读取数据
        Vector<FTPFile> tempfiles = new Vector<>();

        String line = null;
        while ((line = mControlReader.readLine()) != null) {
            if (line.equals("end of files"))
                break;
            Log.d(Constants.TAG, line);
            FTPFile temp = new FTPFile();
            setFtpFileInfo(temp, line);
            tempfiles.add(temp);
        }
        // 读取命令响应
        response = mControlReader.readLine();
        Log.d(Constants.TAG, response);
        FTPFile[] files = new FTPFile[tempfiles.size()];
        tempfiles.copyInto(files);
        return files;
    }

    /**
     * 将字符串解析为 FTPFile
     * */
    private void setFtpFileInfo(FTPFile in, String info) {
        String infos[] = info.split(" ");
        Vector<String> vinfos = new Vector<>();
        for (int i = 0; i < infos.length; i++) {
            if (!infos[i].equals(""))
                vinfos.add(infos[i]);
        }
        in.setName(vinfos.get(8));
        in.setSize(Integer.parseInt(vinfos.get(4)));
        String type = info.substring(0, 1);
        if (type.equals("d")) {
            in.setType(1);
        } else {
            in.setType(0);
        }
    }

      /**
     * 上传指定路径的文件
     * @param  filePath 文件路径
     * @param curDir
     * */
    public void upload(String filePath,String curDir) throws Exception {
        Log.d(Constants.TAG, "File Path :" + filePath);
        File f = new File(filePath);
        if (!f.exists()) {
            Log.d(Constants.TAG, "File not Exists...");
            return;
        }
        // 发送PORT命令
        String url = NetworkUtils.getLocalIpAddress(ClientApplication.getAppContext());
        int dataport = (int) (Math.random() * 100000 % 9999) + 1024;
        String portCommand = "PORT " + url + "," + dataport;
        mControlOut.println(portCommand);
        String response;
        response = mControlReader.readLine();
        Log.d(Constants.TAG, response);
        if(f.isFile()) {
            // 发送命令 STOR
            mControlOut.println("STOR " + f.getName());
            // 打开数据连接
            ServerSocket dataSocketServ = new ServerSocket(dataport);
            Socket dataSocket = dataSocketServ.accept();
            // 读取命令响应
            response = mControlReader.readLine();
            Log.d(Constants.TAG, response);
            // 从服务器读取数据
            BufferedOutputStream output = new BufferedOutputStream(dataSocket.getOutputStream());
            FileInputStream is = new FileInputStream(f);
            BufferedInputStream input = new BufferedInputStream(is);
            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
            input.close();
            output.close();
            dataSocket.close();
            response = mControlReader.readLine();
            Log.d(Constants.TAG, response);
        }else{
            //创建文件夹
            mControlOut.println("STOR " + f.getName()+":d");
            response = mControlReader.readLine();
            Log.d(Constants.TAG, response);
            response = mControlReader.readLine();
            Log.d(Constants.TAG, response);
            //更改目录
            cwd(f.getName());
            //上传文件
            File[] folderFiles = f.listFiles();
            for(int i=0;i<folderFiles.length;i++){
                File item = folderFiles[i];
                if(item.isFile()){
                    upload(item.getAbsolutePath(),curDir);
                }
            }
            cwd(curDir);
        }
    }

    /**
     * 从ftp服务器下载文件
     * @param fileName 下载文件名
     * @param destPath 保存下载文件的路径
     * @param isDir 下载的文件是否为目录
     * */
    public boolean download(String fileName, String destPath,boolean isDir) throws Exception {
        // 发送PORT命令
        String url = NetworkUtils.getLocalIpAddress(ClientApplication.getAppContext());
        int dataPort = (int) (Math.random() * 100000 % 9999) + 1024;
        String portCommand = "PORT " + url + "," + dataPort;
        mControlOut.println(portCommand);
        String response;
        response = mControlReader.readLine();
        Log.d(Constants.TAG, "1="+response);
        if(!isDir) {
            // 从服务器读取数据
            // 发送RETR命令
            mControlOut.println("RETR " + fileName);
            // 创建用于传输文件数据的data socket
            ServerSocket dataSocketServ = new ServerSocket(dataPort);
            Socket dataSocket = dataSocketServ.accept();

            BufferedOutputStream output = new BufferedOutputStream(
                    new FileOutputStream(new File(destPath, fileName)));
            BufferedInputStream input = new BufferedInputStream(
                    dataSocket.getInputStream());
            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            //读写文件
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
            output.close();
            input.close();
            dataSocket.close();
            response = mControlReader.readLine();
            Log.d(Constants.TAG, "2="+response);
            if(response.startsWith("221")){
                return false;
            }
            response = mControlReader.readLine();
            if(response.startsWith("221")){
                return false;
            }
            Log.d(Constants.TAG, "3="+response);
            return true;
        }else{ // 目录
            //创建用于下载目录的文件夹文件
            String destDir = destPath+File.separator+fileName;
            File destDirFile = new File(destDir);
            if(!destDirFile.exists()){
                destDirFile.mkdirs();//创建目录
            }
            FTPFile[] files = getFolderFiles(File.separator+fileName);
            //下载文件夹中的文件
            if(files.length > 0){
                for(int i=0;i<files.length;i++) {
                    FTPFile ftpFile = files[i];
                    if(ftpFile.isFile()) {
                        download(fileName+File.separator+
                                ftpFile.getName(),destPath,false);
                    }
                }
            }
            return true;
        }
    }

    /**
     * 从 ftp 服务器退出
     * */
    public boolean quit() throws Exception {
        String response;
        // 发送 QUIT 命令
        mControlOut.println("QUIT");
        // 读取命令响应
        response = mControlReader.readLine();
        Log.d(Constants.TAG, response);
        if(response.startsWith("221")){
            Log.d(Constants.TAG,"quit ====================");
            mControlReader.close();
            mControlOut.close();
            mSocket.close();
            return true;
        }
        return  false;
    }

    /**
    * 更改当前目录
    * */
    public boolean cwd(String dir) throws Exception
    {
        String response;
        // 发送CWD命令
        mControlOut.println("CWD "+dir);
        // 读取命令响应
        response = mControlReader.readLine();
        Log.d(Constants.TAG, response);
        if(response.startsWith("250")){
            return true;
        }
        return  false;
    }
}