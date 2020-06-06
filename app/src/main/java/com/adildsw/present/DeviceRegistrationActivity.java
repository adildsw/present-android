package com.adildsw.present;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceRegistrationActivity extends AppCompatActivity {

    DataUtils dUtils;
    TextView textWelcomeReg;
    EditText etStudentRoll;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_registration);

        dUtils = new DataUtils(getApplicationContext());

        textWelcomeReg = findViewById(R.id.textWelcomeReg);
        etStudentRoll = findViewById(R.id.etStudentRoll);
        btnRegister = findViewById(R.id.btnRegister);

        // Updating welcome text
        String orgName = dUtils.readDataFromSP("orgName");
        String welcomeText = "WELCOME TO\n" + orgName.toUpperCase();
        textWelcomeReg.setText(welcomeText);

        // Checking for error message in Intent Extras
        String message = "";
        String studentRoll = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = extras.getString("message");
            studentRoll = extras.getString("studentRoll");
            if (message == null) {
                message = "";
            }
            if (studentRoll == null) {
                studentRoll = "";
            }
        }
        if (!message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        etStudentRoll.setText(studentRoll);

        btnRegister.setEnabled(!etStudentRoll.getText().toString().isEmpty());

        // Click event to check server connectivity
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentRoll = etStudentRoll.getText().toString();

                Intent intent = new Intent(DeviceRegistrationActivity.this,
                        LoaderActivity.class);
                intent.putExtra("studentRoll", studentRoll);
                intent.putExtra("loaderMode", "DEVICE_REGISTRATION");
                startActivity(intent);
            }
        });

        // Disabling btnRegister if etStudentRoll empty
        etStudentRoll.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etStudentRoll.getText().toString().isEmpty()) {
                    btnRegister.setEnabled(false);
                } else {
                    btnRegister.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


}
