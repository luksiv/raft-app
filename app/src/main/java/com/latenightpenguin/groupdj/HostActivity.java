package com.latenightpenguin.groupdj;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Random;

public class HostActivity extends AppCompatActivity {

    // Static variables
    private static int ROOM_ID_LENGHT = 4; // 4 will do for now, later on we can change it

    // Declaring variables
    String mRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // Generate room id with uppercase letters and numbers
        mRoomId = new RandomString(ROOM_ID_LENGHT).nextString();

        // Shows a toast message with room id
        Toast.makeText(this,"Room id is :" + mRoomId, Toast.LENGTH_LONG).show();
    }
}
