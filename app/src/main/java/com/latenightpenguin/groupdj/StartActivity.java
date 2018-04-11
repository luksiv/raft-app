package com.latenightpenguin.groupdj;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
                try {
                int roomId = Integer.parseInt(((EditText) findViewById(R.id.et_RoomId)).getText().toString());
                // Creates a new intent to ClientActivity and starts it
                Intent intent = new Intent(StartActivity.super.getApplicationContext(), ClientActivity.class);
                // Put roomId to intent
                intent.putExtra("roomId", roomId);
                // Hiding the keyboard to make the transition more clean
                hideKeyboard(StartActivity.this);
                startActivity(intent); } catch (NumberFormatException e){
                    Toast.makeText(StartActivity.this, "Incorrect room id format.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // A method to hide the keyboard
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
