package com.latenightpenguin.groupdj.NetworkServices;

public class ServerRequest {
    private String path;
    private String body;
    private String method;
    private Callback callback;
    private String[] arguments;

    public ServerRequest(String method, String path, String body, Callback callback, String... arguments) {
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

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public interface Callback {
        void execute();
    }
}
