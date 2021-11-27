package com.me.server.command;

import android.content.Context;
import android.util.Log;

import com.me.server.common.Constants;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 数据池
 * 用于存储系统的公共数据，包括系统配置
 * 信息等
 * */
public class PublicDataPool {
    //文件系统的根路径
    public static  String sRootDir = new File("").getAbsolutePath();
    //配置的用户信息
    public static Map<String,String> sUsers = new HashMap<>();
    //登录用户信息
    public static HashSet<String> sLoginUser = new HashSet<>();

    //初始化服务器配置信息，包括用户账号信息、初始路径
    public static void init(Context context){
        try {
            //读取配置文件
            InputStream in = context.getAssets().open("server.xml");
            SAXBuilder builder = new SAXBuilder();
            Document parse = builder.build(in);
            Element root = parse.getRootElement();
            //配置根路径名
            String rootPath = root.getChildText("rootDir");
            File rootFile = context.getExternalFilesDir(rootPath);
            sRootDir = rootFile.getAbsolutePath();
            if(!rootFile.exists()){
                rootFile.mkdirs();
            }
            Log.d(Constants.TAG,"rootDir is:"+sRootDir);
            Element usersE = root.getChild("users");
            List<Element> usersEC = usersE.getChildren();
            Log.d(Constants.TAG,"All User Info:");
            for(Element user : usersEC) {
                String username = user.getChildText("username");
                String password = user.getChildText("password");
                Log.d(Constants.TAG,"username:"+username);
                Log.d(Constants.TAG,"password:"+password);
                sUsers.put(username,password);
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}  