package com.adildsw.present;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ServerErrorActivity extends AppCompatActivity {

    DataUtils dUtils;

    Button btnRetry, btnNewServerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_error);

        dUtils = new DataUtils(getApplicationContext());

        btnRetry = (Button) findViewById(R.id.btnRetry);
        btnNewServerAddress = (Button) findViewById(R.id.btnNewServerAddress);

        SharedPreferences sharedPreferences = getSharedPreferences("presentPreference",
                Context.MODE_PRIVATE);
        String serverAddress = sharedPreferences.getString("serverAddress", "");

        // Opening LoaderActivity to retry connecting to server
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverAddress = dUtils.readDataFromSP("lastFailedServerAddress");
                Intent intent = new Intent(ServerErrorActivity.this,
                        LoaderActivity.class);
                intent.putExtra("serverAddress", serverAddress);
                startActivity(intent);
            }
        });

        // Opening MainActivity to change server address
        btnNewServerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ServerConnectionActivity.class));
            }
        });
    }
}
