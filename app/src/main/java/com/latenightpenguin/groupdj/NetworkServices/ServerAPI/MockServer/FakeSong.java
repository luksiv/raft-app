package com.latenightpenguin.groupdj.NetworkServices.ServerAPI.MockServer;

class FakeSong {
    private String id;
    private int pos;

    public FakeSong(String id, int pos) {
        this.id = id;
        this.pos = pos;
    }

    public String getId() {
        return id;
    }

    public int getPos() {
        return pos;
    }
}
