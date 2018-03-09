package com.latenightpenguin.groupdj;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ClientActivity extends AppCompatActivity {

    private static final String TAG = ClientActivity.class.getSimpleName();
    // Declaring view elements
    ImageButton btnAdd;

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
        btnAdd = findViewById(R.id.btn_AddSong);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientActivity.super.getApplicationContext(), AddSongActivity.class);
                startActivity(intent);
            }
        });

    }
}
