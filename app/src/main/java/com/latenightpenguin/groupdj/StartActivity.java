package com.latenightpenguin.groupdj;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = StartActivity.class.getSimpleName();

    // View variables
    Button btn_Host;
    Button btn_Join;
    Button btn_Login;
    EditText et_RoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Assigning a button to btn_Host and setting an OnClickListener for it
        btn_Host = (Button) findViewById(R.id.btn_HostRoom);
        btn_Host.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creates a new intent to HostActivity and starts it
                Intent intent = new Intent(StartActivity.super.getApplicationContext(),
                        HostActivity.class);
                startActivity(intent);
            }
        });

        // Assigning a button to btn_Join and setting an OnClickListener for it
        btn_Join = (Button) findViewById(R.id.btn_JoinRoom);
        btn_Join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Getting the room id
                String roomId = ((EditText) findViewById(R.id.et_RoomId)).getText().toString();
                if(roomId.length() == getResources().getInteger(R.integer.room_id_lenght)) {
                    // Creates a new intent to ClientActivity and starts it
                    Intent intent = new Intent(StartActivity.super.getApplicationContext(),
                            ClientActivity.class);
                    // Put roomId to intent
                    intent.putExtra("roomId", roomId.toUpperCase());
                    startActivity(intent);
                } else {
                    // Shows a toast message informing that the room id is invalid
                    Toast.makeText(StartActivity.super.getApplicationContext(),
                            "Invalid Room ID",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
