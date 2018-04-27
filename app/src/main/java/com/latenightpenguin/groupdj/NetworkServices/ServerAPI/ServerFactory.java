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

    public static AdditionalCallbacks getAdditionalCallbacks(IServerHelper helper){
        if(helper == null){
            return null;
        } else if(helper instanceof ServerHelper){
            return null;
        } else if(helper instanceof FakeServerHelper){
            return new AdditionalCallbacks((FakeServerHelper)helper);
        }

        return null;
    }

    public static class AdditionalCallbacks{
        private FakeServerHelper serverHelper;

        public AdditionalCallbacks(FakeServerHelper serverHelper){
            this.serverHelper = serverHelper;
        }

        public void vote(){
            serverHelper.voteAsSomeoneElse();
        }

        public void add(){
            serverHelper.addAsSomeoneElse();
        }

        public void next(){
            serverHelper.playNextAsSomeoneElse();
        }

        public void pause(long milliseconds){
            serverHelper.pauseAsSomeoneElse(milliseconds);
        }

        public void playtime(long milliseconds){
            serverHelper.playTimeAsSomeoneElse(milliseconds);
        }
    }

    public enum FactoryOptions{
        NORMAL,
        FAKE
    }
}
