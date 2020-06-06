package com.adildsw.present;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ServerConnectionActivity extends AppCompatActivity {

    Button btnCheckServer;
    EditText etServerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connection);

        btnCheckServer = findViewById(R.id.btnCheckServer);
        etServerAddress = findViewById(R.id.etServerAddress);

        btnCheckServer.setEnabled(false);

        // Click event to check server connectivity
        btnCheckServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverAddress = etServerAddress.getText().toString();
                if (!serverAddress.startsWith("http")) {
                    serverAddress = "http://" + serverAddress;
                }

                // Sending serverAddress to ServerLoaderActivity for establishing connection
                Intent intent = new Intent(ServerConnectionActivity.this,
                        LoaderActivity.class);
                intent.putExtra("serverAddress", serverAddress);
                startActivity(intent);
            }
        });

        // Disabling btnCheckServer if etServerAddress is empty
        etServerAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etServerAddress.getText().toString().isEmpty()) {
                    btnCheckServer.setEnabled(false);
                } else {
                    btnCheckServer.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
