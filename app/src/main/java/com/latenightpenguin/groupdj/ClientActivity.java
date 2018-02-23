package com.latenightpenguin.groupdj;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class ClientActivity extends AppCompatActivity {

    private static final String TAG = ClientActivity.class.getSimpleName();

    String mRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        try{
            mRoomId = getIntent().getStringExtra("roomId");
            ((TextView) findViewById(R.id.tw_RoomId)).setText(mRoomId);
        } catch (Exception e){
            Log.v("ClientActivity", e.getMessage());
        }

    }
}
