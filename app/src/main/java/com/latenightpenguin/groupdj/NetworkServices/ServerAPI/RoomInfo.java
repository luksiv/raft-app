package com.latenightpenguin.groupdj.NetworkServices.ServerAPI;

public class RoomInfo {
    private int loginCode;
    private int id;

    public RoomInfo() {
        loginCode = -1;
        id = -1;
    }

    public int getLoginCode() {
        return loginCode;
    }

    public void setLoginCode(int loginCode) {
        this.loginCode = loginCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
