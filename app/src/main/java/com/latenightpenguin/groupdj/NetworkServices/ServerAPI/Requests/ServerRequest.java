package com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests;

import android.telecom.Call;
import android.view.View;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.ICallback;

import java.io.InputStream;

public class ServerRequest {
    private String path;
    private String body;
    private String method;
    private ICallback callback;
    private String[] arguments;

    public ServerRequest(String method, String path, String body, ICallback callback, String... arguments) {
        this.method = method;
        this.path = path;
        this.body = body;
        this.callback = callback;
        this.arguments = arguments;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public ICallback getCallback() {
        return callback;
    }

    public void setCallback(ICallback callback) {
        this.callback = callback;
    }

    public String[] getArguments() {
        return arguments;
    }

    public String getArgumentsFormatted() {
        StringBuilder formattedArguments = new StringBuilder();
        formattedArguments.append("?");

        for(int i = 0; i < arguments.length; i++) {
            formattedArguments.append(arguments[i]);

            if(i < arguments.length - 1) {
                formattedArguments.append("&");
            }
        }

        return formattedArguments.toString();
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
}
