package com.adildsw.present;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class StudentDashboardActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener{

    private static final int CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        loadFragment(new StudentDetailsFragment());

        getSupportActionBar().setTitle("Student Details");

        BottomNavigationView navigation = findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
        switch (menuItem.getItemId()) {
            case R.id.btnStudentDetails:
                fragment = new StudentDetailsFragment();
                getSupportActionBar().setTitle("Student Details");
                break;
            case R.id.btnScanQR:
                if (checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)) {
                    fragment = new ScanQRCodeFragment();
                    getSupportActionBar().setTitle("Scan QR Code");
                }
                break;
            case R.id.btnHostControls:
                fragment = new HostControlsFragment();
                getSupportActionBar().setTitle("Host Controls");
                break;
        }
        return loadFragment(fragment);
    }

    /**
     * Loads fragment into the fragment container.
     *
     * @param fragment  contains the fragment to be loaded
     * @return          returns true if a fragment is loaded, returns false otherwise
     */
    public boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * Checks for permission and requests permission if the permission is denied by default
     *
     * @param permission    contains permission string which is to be checked
     * @param requestCode   contains request code unique to each permission
     * @return              returns true if permission is granted, otherwise returns false
     */
    public boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(StudentDashboardActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {
            // Requesting permission
            ActivityCompat.requestPermissions(StudentDashboardActivity.this,
                    new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    /**
     * Redirects to ScanQRCodeFragment if camera permission is granted, otherwise displays relevant
     * toast message on the screen.
     *
     * @param requestCode   contains request code unique to each permission
     * @param permissions   contains permission array string
     * @param grantResults  contains results of each permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Fragment fragment = new ScanQRCodeFragment();
                getSupportActionBar().setTitle("Scan QR Code");
                loadFragment(fragment);
            }
        }
        else {
            Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
