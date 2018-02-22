package com.latenightpenguin.groupdj;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    // View variables
    Button btn_Host;

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

    }
}
