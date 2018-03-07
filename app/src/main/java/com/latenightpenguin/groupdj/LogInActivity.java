package com.latenightpenguin.groupdj;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class LogInActivity extends AppCompatActivity {

    private static final String UserName = "";
    private static final String Password = "";
    private static final String mypreference = "";
    Button btn_LogIn;
    EditText et_Password;
    EditText et_UserName;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_login);

        // Assigning a edit text fields for et_UserName and et_Password
        et_UserName = (EditText) findViewById(R.id.et_UserName);
        et_Password = (EditText) findViewById(R.id.et_Password);

        // Retrieving sharedPreferences data
        sharedPreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        // Getting values and putting them back on screen
        et_UserName.setText(sharedPreferences.getString(UserName, null));
        et_Password.setText(sharedPreferences.getString(Password, null));
        // Assigning a button to btn_Host and setting an OnClickListener for it
        btn_LogIn = (Button) findViewById(R.id.btn_LogIn);
        btn_LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeInfo();
            }
        });
    }
    protected void writeInfo(){
        // Opens the editor
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Puts UserName and Password values
        editor.putString(UserName, et_UserName.getText().toString());
        editor.putString(Password, et_Password.getText().toString());
        // Commits the changes
        editor.commit();
    }
}
