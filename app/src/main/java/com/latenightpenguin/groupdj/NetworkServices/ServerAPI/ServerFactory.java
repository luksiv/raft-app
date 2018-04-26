package com.latenightpenguin.groupdj.NetworkServices.ServerAPI;

public class ServerFactory {
    public static IServerHelper make(String url){
        return new ServerHelper(url);
    }

    public static IServerHelper make(String url, FactoryOptions option){
        if(option == FactoryOptions.NORMAL){
            return new ServerHelper(url);
        } else if(option == FactoryOptions.FAKE){
            return null;
        }

        return null;
    }

    public enum FactoryOptions{
        NORMAL,
        FAKE
    }
}
