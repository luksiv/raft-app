package com.latenightpenguin.groupdj.NetworkServices.ServerAPI.MockServer;

class FakeRoom {
    private int id;
    private int loginCode;
    private int users;
    private int songIndex;
    private int voteOut;
    private double threshHold;

    public FakeRoom(int id, int loginCode, int users) {
        this.id = id;
        this.loginCode = loginCode;
        this.users = users;
        songIndex = 1;
        voteOut = 0;
        threshHold = 0.5;
    }

    public int getId() {
        return id;
    }

    public int getLoginCode() {
        return loginCode;
    }

    public int getUsers() {
        return users;
    }

    public int getSongIndex() {
        return songIndex;
    }

    public void setSongIndex(int songIndex) {
        this.songIndex = songIndex;
    }

    public int getVoteOut() {
        return voteOut;
    }

    public void setVoteOut(int voteOut) {
        this.voteOut = voteOut;
    }

    public double getThreshHold() {
        return threshHold;
    }

    public void setThreshHold(double threshHold) {
        this.threshHold = threshHold;
    }
}
