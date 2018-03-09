package com.latenightpenguin.groupdj;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Random;

public class HostActivity extends AppCompatActivity {
    private static final String TAG = HostActivity.class.getSimpleName();
    // Declaring variables
    String mRoomId;
    // Declaring view elements
    ImageButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // Generate room id with uppercase letters and numbers
        mRoomId = new RandomString(getResources().getInteger(R.integer.room_id_lenght)).nextString();
        // Shows a toast message with room id

        Toast.makeText(this, "Room id is :" + mRoomId, Toast.LENGTH_LONG).show();
        try {
            // Sets textview's text to room id
            TextView tw_RoomId = (TextView) findViewById(R.id.tw_RoomId);
            tw_RoomId.setText(mRoomId);
        } catch (Exception e){
            Log.e("HostActivity", e.getMessage());
        }
        btnAdd = findViewById(R.id.btn_AddSong);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HostActivity.super.getApplicationContext(), AddSongActivity.class);
                startActivity(intent);
            }
        });
    }
}
