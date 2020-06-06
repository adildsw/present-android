package com.adildsw.present;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoaderActivity extends AppCompatActivity {

    DataUtils dUtils;

    TextView tvLoadStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);

        dUtils = new DataUtils(getApplicationContext());

        tvLoadStatus = findViewById(R.id.tvLoadStatus);

        String serverAddress = "";
        String loaderMode = "CHECK_SERVER";
        String studentRoll = "";
        @SuppressLint("HardwareIds") String deviceUID = Settings.Secure.getString(
                getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            serverAddress = extras.getString("serverAddress");
            loaderMode = extras.getString("loaderMode");
            studentRoll = extras.getString("studentRoll");
            if (serverAddress == null) {
                serverAddress = "";
            }
            if (loaderMode == null) {
                loaderMode = "CHECK_SERVER";
            }
            if (studentRoll == null) {
                studentRoll = "";
            }
        }

        // Initializing writeFlag for writing serverAddress if it is accessible
        boolean serverAddressWriteFlag = !serverAddress.isEmpty();

        // Retrieving serverAddress from SharedPreferences if not available in Intent Extras
        if (serverAddress.isEmpty()) {
            serverAddress = dUtils.readDataFromSP("serverAddress");
        }

        switch (loaderMode) {
            case "CHECK_SERVER":
                checkServerConnection(serverAddress, serverAddressWriteFlag, deviceUID);
                break;
            case "DEVICE_REGISTRATION":
                registerDevice(serverAddress, deviceUID, studentRoll, "CHECK");
                break;
        }
    }

    /**
     * Checks connection to server. If server connection is established, then checks for device
     * registration, otherwise redirects to ServerErrorActivity. If no server address is provided,
     * then redirects to ServerConnectionActivity.
     *
     * @param serverAddress contains server address to check connection to
     * @param writeFlag     states whether server info should be written to SharedPreferences
     *                      if connection is established
     * @param deviceUID     contains device id for checking device registration after connection
     */
    public void checkServerConnection(final String serverAddress, final boolean writeFlag,
                                      final String deviceUID) {
        tvLoadStatus.setText("Establishing Connection...");
        if (serverAddress.isEmpty()) {
            startActivity(new Intent(LoaderActivity.this,
                    ServerConnectionActivity.class));
        } else {
            String url = serverAddress + "/org_name";
            RequestQueue queue = Volley.newRequestQueue(LoaderActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equals("None")) {
                                startErrorActivity(serverAddress);
                            } else {
                                if (writeFlag) {
                                    dUtils.writeDataInSP("serverAddress", serverAddress);
                                    dUtils.writeDataInSP("lastFailedServerAddress", serverAddress);
                                    dUtils.writeDataInSP("orgName", response);
                                }
                                checkDeviceRegistration(serverAddress, deviceUID);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            startErrorActivity(serverAddress);
                        }
                    });

            queue.add(stringRequest);
        }
    }

    /**
     * Checks whether the device is registered in the given server address. Redirects to application
     * homepage if the device is registered, otherwise redirects to DeviceRegistrationActivity.
     * Redirects to ServerErrorActivity in case of errors.
     *
     * @param serverAddress contains server address for checking device registration
     * @param deviceUID     contains device id for checking registration against
     */
    public void checkDeviceRegistration(final String serverAddress, final String deviceUID) {
        tvLoadStatus.setText("Checking Device Registration...");

        HashMap<String, String> params = new HashMap<>();
        params.put("device_uid", deviceUID);

        String url = serverAddress + "/device_info";
        RequestQueue queue = Volley.newRequestQueue(LoaderActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.get("status").toString();
                            if (status.equals("S0")) { // Load application home
                                updateStudentData(response);
                                startActivity(new Intent(LoaderActivity.this,
                                        HomeActivity.class));
                            } else {
                                startDeviceRegistrationActivity("", "");
                            }
                        } catch (JSONException e) {
                            startErrorActivity(serverAddress);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        startErrorActivity(serverAddress);
                    }
                });

        queue.add(jsonObjectRequest);
    }

    /**
     * Registers device on the server by linking it with the specified studentRoll.
     *
     * @param serverAddress contains server address for device registration
     * @param deviceUID     contains device id for registering
     * @param studentRoll   contains the student roll number to be linked with the deviceUID
     * @param mode          contains the register mode, i.e., mode="CHECK" when checking if
     *                      registration is possible; mode="REGISTER" when confirming registration
     */
    public void registerDevice(final String serverAddress, final String deviceUID,
                               final String studentRoll, final String mode) {
        tvLoadStatus.setText("Registering Device...");

        HashMap<String, String> params = new HashMap<>();
        params.put("device_uid", deviceUID);
        params.put("student_roll", studentRoll);
        params.put("mode", mode);

        String url = serverAddress + "/register_device";
        RequestQueue queue = Volley.newRequestQueue(LoaderActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.get("status").toString();
                            String message = "";
                            switch (status) {
                                case "S0":
                                    if (mode.equals("REGISTER")) {
                                        updateStudentData(response);
                                        startActivity(new Intent(LoaderActivity.this,
                                                HomeActivity.class));
                                    } else if (mode.equals("CHECK")) {
                                        preRegistrationConfirmation(response, serverAddress,
                                                deviceUID);
                                    }
                                    break;
                                case "S1":
                                    message = "Please enter a valid roll number.";
                                    break;
                                case "E11":
                                    message = "The entered roll number is already linked with " +
                                            "another device. If this is you, please contact the " +
                                            "system administrator.";
                                    break;
                                default:
                                    startErrorActivity(serverAddress);
                                    break;
                            }

                            if (!message.isEmpty()) {
                                startDeviceRegistrationActivity(studentRoll, message);
                            }
                        } catch (JSONException e) {
                            startErrorActivity(serverAddress);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        startErrorActivity(serverAddress);
                    }
                });

        queue.add(jsonObjectRequest);
    }

    /**
     * Launches an alert box displaying the student information for confirmation before linking the
     * device to the given student ID.
     *
     * @param studentInformation    contains the student's information for confirmation
     * @param serverAddress         contains the server address for linking device
     * @param deviceUID             contains the deviceUID to be linked to the student ID
     * @throws JSONException        throws JSONException
     */
    public void preRegistrationConfirmation(final JSONObject studentInformation,
                                           final String serverAddress, final String deviceUID)
            throws JSONException {
        tvLoadStatus.setText("Registering Device...");

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoaderActivity.this);
        alertDialog.setTitle("Verify Information");
        alertDialog.setMessage(Html.fromHtml(
                String.format(
                        "<b>Student Name:</b> %1$s<br>" +
                                "<b>Roll Number:</b> %2$s<br>" +
                                "<b>Department:</b> %3$s<br>" +
                                "<b>Section:</b> %4$s<br>" +
                                "<b>Start Year:</b> %5$s<br>" +
                                "<b>Graduation Year:</b> %6$s<br><br>" +
                                "Is this information correct?",
                        studentInformation.get("student_name").toString(),
                        studentInformation.get("student_roll").toString(),
                        studentInformation.get("dept_name").toString(),
                        studentInformation.get("section").toString(),
                        studentInformation.get("start_year").toString(),
                        studentInformation.get("grad_year").toString()
                ),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
        );
        final String studentRoll = studentInformation.get("student_roll").toString();
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder cnfAlertDialog = new AlertDialog.Builder(
                        LoaderActivity.this);
                cnfAlertDialog.setTitle("Confirm Device Registration");
                cnfAlertDialog.setMessage(Html.fromHtml(
                        "Are you sure you want to link your device to this ID?<br><br>" +
                                "<b>WARNING: This action is irreversible.</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY)
                );
                cnfAlertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        registerDevice(serverAddress, deviceUID, studentRoll, "REGISTER");
                    }
                });
                cnfAlertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startDeviceRegistrationActivity(studentRoll, "");
                    }
                });
                cnfAlertDialog.show();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startDeviceRegistrationActivity(studentRoll, "");
            }
        });
        alertDialog.show();
    }

    /**
     * Writes the failed server address in SharedPreferences and starts the ServerErrorActivity.
     *
     * @param failedServerAddress contains the server address of the server with which connection
     *                            could not be established
     */
    public void startErrorActivity(String failedServerAddress) {
        dUtils.writeDataInSP("lastFailedServerAddress", failedServerAddress);
        startActivity(new Intent(LoaderActivity.this, ServerErrorActivity.class));
    }

    /**
     * Launches the deviceRegistrationActivity with message and studentRoll parameters.
     *
     * @param studentRoll   student roll number which is passed to the deviceRegistrationActivity
     * @param message       contains toast message which is passed to the deviceRegistrationActivity
     */
    public void startDeviceRegistrationActivity(@Nullable String studentRoll,
                                               @Nullable String message) {
        Intent intent = new Intent(LoaderActivity.this,
                DeviceRegistrationActivity.class);
        intent.putExtra("message", message);
        intent.putExtra("studentRoll", studentRoll);
        startActivity(intent);
    }

    /**
     * Resolves response from server containing studentRes and writes/updates to SharedPreferences.
     *
     * @param studentRes JSON Object obtained from server containing student information
     * @throws JSONException throws JSONException
     */
    public void updateStudentData(JSONObject studentRes) throws JSONException {
        String studentName = studentRes.get("student_name").toString();
        String studentRoll = studentRes.get("student_roll").toString();
        String classCode = studentRes.get("class_code").toString();
        String deptName = studentRes.get("dept_name").toString();
        String deptCode = studentRes.get("dept_code").toString();
        String startYear = studentRes.get("start_year").toString();
        String gradYear = studentRes.get("grad_year").toString();
        String section = studentRes.get("section").toString();
        String deviceUID = studentRes.get("device_uid").toString();

        dUtils.writeDataInSP("studentName", studentName);
        dUtils.writeDataInSP("studentRoll", studentRoll);
        dUtils.writeDataInSP("classCode", classCode);
        dUtils.writeDataInSP("deptName", deptName);
        dUtils.writeDataInSP("deptCode", deptCode);
        dUtils.writeDataInSP("startYear", startYear);
        dUtils.writeDataInSP("gradYear", gradYear);
        dUtils.writeDataInSP("section", section);
        dUtils.writeDataInSP("deviceUID", deviceUID);
    }

}
