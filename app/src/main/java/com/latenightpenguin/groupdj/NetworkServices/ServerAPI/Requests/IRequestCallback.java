package com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests;

public interface IRequestCallback {
    void onSuccess(String response);
    void onError(int code, String message);
}
