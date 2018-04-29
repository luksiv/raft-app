package com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests;

import android.os.AsyncTask;
import android.util.Log;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.ServerHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ConnectionManager extends AsyncTask<ServerRequest, Void, String> {
    ServerRequest[] requests;
    int responseCode;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(ServerRequest... requests) {
        this.requests = requests;
        InputStream response;
        StringBuilder responseString = new StringBuilder();

        try {
            URL url;
            if(requests[0].getArguments() != null && requests[0].getArguments().length > 0) {
                url = new URL(RequestsHelper.serverUrl + requests[0].getPath() + requests[0].getArgumentsFormatted());
            } else {
                url = new URL(RequestsHelper.serverUrl + requests[0].getPath());
            }

            Log.d("MusicDJAsync", url.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requests[0].getMethod());
            connection.setRequestProperty("Content-Type", "application/json");

            if(requests[0].getBody() != null) {
                connection.setDoOutput(true);

                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(out);
                writer.write(requests[0].getBody());
                writer.close();
                out.close();
            }

            connection.connect();

            Log.d("MusicDJ", "Response code: " + connection.getResponseCode());
            responseCode = connection.getResponseCode();
            response = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(response));

            String line = "";
            try{
                while((line = reader.readLine()) != null){
                    responseString.append(line).append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            Log.d("MusicDJ", e.getMessage());
            responseString.append(ServerHelper.RESPONSE_ERROR);
            e.printStackTrace();
        }

        return responseString.toString();
    }

    @Override
    protected void onPostExecute(String result){
        if(requests[0].getCallback() != null) {
            if(responseCode == 200) {
                requests[0].getCallback().onSuccess(result);
            }
            else{
                requests[0].getCallback().onError(responseCode, result);
            }
        }
    }
}
