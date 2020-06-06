package com.adildsw.present;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    DataUtils dUtils;

    TextView tvWelcomeStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dUtils = new DataUtils(getApplicationContext());

        tvWelcomeStudent = findViewById(R.id.tvWelcomeStudent);

        String studentName = dUtils.readDataFromSP("studentName");
        String welcomeString = "WELCOME \n" + studentName.toUpperCase();
        tvWelcomeStudent.setText(welcomeString);

        clickAction(null);

    }

    public void clickAction(@Nullable View view) {
        startActivity(new Intent(HomeActivity.this, StudentDashboardActivity.class));
    }
}
