package com.me.server.command;
/**
 * FTP 命令
 * 根据要求命令分配给不同的处理器
 * */
public class CommandFactory {

    public static Command createCommand(String type) {

        type = type.toUpperCase();

        switch(type) {
            case "USER":return new UserCommand();

            case "PASS":return new PassCommand();

            case "LIST":return new DirCommand();

            case "PORT":return new PortCommand();

            case "QUIT":return new QuitCommand();

            case "RETR":return new RetrCommand();

            case "CWD":return new CwdCommand();

            case "STOR":return new StoreCommand();

            default : return null;
        }
    }
}