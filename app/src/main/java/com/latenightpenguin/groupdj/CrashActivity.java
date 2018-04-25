package com.latenightpenguin.groupdj;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler());
        ErrorHandler.setContext(CrashActivity.this);
        setContentView(R.layout.activity_crash);

        Button refresh_button = (Button) findViewById(R.id.refresh_button);
        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creates a new intent to HostActivity and starts it
                Intent intent = new Intent(CrashActivity.super.getApplicationContext(),
                        StartActivity.class);
                startActivity(intent);
            }
        });

    }
}
