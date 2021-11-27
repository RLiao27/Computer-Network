package com.me.server.command;

import android.util.Log;

import com.me.server.common.Constants;

import java.io.IOException;
import java.io.Writer;

public class PortCommand implements Command {
    @Override
    public void getResult(String data, Writer writer, ContextThread t) {
        String response = "200 the port an ip have been transferred";
        try {
            String[] iAp =  data.split(",");
            String ip = iAp[0];
            String port = Integer.toString(Integer.parseInt(iAp[1]));
            Log.d(Constants.TAG,"ip is "+ip);
            Log.d(Constants.TAG,"port is "+port);
            t.setDataIp(ip);
            t.setDataPort(port);
            writer.write(response);
            writer.write("\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}  