package com.latenightpenguin.groupdj.NetworkServices.ServerWebSockets;

import android.util.Log;

import com.latenightpenguin.groupdj.ErrorHandler;
import com.latenightpenguin.groupdj.NetworkServices.ServerRequest;

import java.lang.annotation.Annotation;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit.http.QueryMap;

public class ServerListener extends WebSocketListener {
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private MessageHandler messageHandler;
    private MessageHandler errorHandler;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        Log.d("WebSocket", text);

        if(messageHandler != null) {
            messageHandler.handle(text);
        }

        /* (text) {
            case "song added":
                songAddedCallback.execute("");
                break;
            case "next song":
                playingNextCallback.execute("");
                break;
            default:
                String[] firstPart = text.split(":");
                if(firstPart[0].equals("paused")) {
                    songPausedCallback.execute(firstPart[1] + ":" + firstPart[2]);
                } else {
                    songPlayTimeCallback.execute(text);
                }
                break;
        }*/
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.d("WebSocket", "Error connecting to server " + response);
        errorHandler.handle(t.getMessage());
        //throw new Exception("Can't connect to server");
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void setErrorHandler(MessageHandler errorHandler) {
        this.errorHandler = errorHandler;;
    }

    public interface MessageHandler {
        void handle(String message);
    }
}
