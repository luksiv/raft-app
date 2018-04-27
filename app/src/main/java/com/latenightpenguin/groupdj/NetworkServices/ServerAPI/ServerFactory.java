package com.latenightpenguin.groupdj.NetworkServices.ServerAPI;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.MockServer.FakeServerHelper;

public class ServerFactory {
    public static IServerHelper make(String url){
        return new ServerHelper(url);
    }

    public static IServerHelper make(String url, FactoryOptions option){
        if(option == FactoryOptions.NORMAL){
            return new ServerHelper(url);
        } else if(option == FactoryOptions.FAKE){
            return new FakeServerHelper();
        }

        return null;
    }

    public enum FactoryOptions{
        NORMAL,
        FAKE
    }
}
