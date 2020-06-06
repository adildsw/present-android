package com.adildsw.present;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ScanQRCodeFragment extends Fragment {

    private CameraSource cameraSource;
    private View view;

    DataUtils dUtils;

    private int attendance_request_lock = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_scan_qrcode, container, false);

        SurfaceView svCameraFeed = view.findViewById(R.id.svCameraFeed);

        dUtils = new DataUtils(view.getContext());

        final Handler handlerAttendanceLock = new Handler();
        final Runnable runnableAttendanceLock = new Runnable() {
            @Override
            public void run() {
                resetAttendanceRequestLock();
            }
        };

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(view.getContext())
                .setBarcodeFormats(Barcode.QR_CODE).build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                if (qrCodes.size() != 0 && attendance_request_lock == 0) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            attendance_request_lock = 1;
                            registerAttendance(qrCodes.valueAt(0).displayValue);
                            handlerAttendanceLock.postDelayed(runnableAttendanceLock,
                                    3000);
                        }
                    });
                }
            }
        });

        cameraSource = new CameraSource.Builder(view.getContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK).build();

        svCameraFeed.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(final SurfaceHolder holder) {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            cameraSource.start(holder);
                            setCardCameraContainerSize(cameraSource.getPreviewSize(),
                                    (CardView) view.findViewById(R.id.cardCameraContainer));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        return view;
    }

    /**
     * Resets attendance request lock.
     */
    private void resetAttendanceRequestLock() {
        attendance_request_lock = 0;
    }

    /**
     * Registers attendance after scanning the QRCode.
     *
     * @param qrCode contains the scanned QRCode string to register the attendance
     */
    private void registerAttendance(String qrCode) {
        HashMap<String, String> params = new HashMap<>();
        params.put("device_uid", dUtils.readDataFromSP("deviceUID"));
        params.put("qrcode_id", qrCode);

        String url = dUtils.readDataFromSP("serverAddress") + "/request_attendance";
        RequestQueue queue = Volley.newRequestQueue(view.getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");
                            String courseCode = response.getString("course_code");
                            String courseName = response.getString("course_name");
                            switch (status) {
                                case "S0":
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
                                    alertDialogBuilder.setTitle("Success");
                                    alertDialogBuilder.setMessage(Html.fromHtml(
                                            String.format("Attendance registered for the " +
                                                            "following course:<br><br>" +
                                                            "<b>Course Code:</b> %1$s<br>" +
                                                            "<b>Course Name:</b> %2$s<br><br>" +
                                                            "Click OK to continue.",
                                                    courseCode, courseName),
                                            HtmlCompat.FROM_HTML_MODE_LEGACY
                                    ));
                                    alertDialogBuilder.setIcon(R.drawable.ic_check_circle_24px);
                                    alertDialogBuilder.setPositiveButton("Ok", null);
                                    alertDialogBuilder.setCancelable(false);
                                    alertDialogBuilder.show();
                                    break;
                                case "S4":
                                case "E10":
                                case "E13":
                                    Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG)
                                            .show();
                                    break;
                                default:
                                    startActivity(new Intent(view.getContext(),
                                            ServerErrorActivity.class));
                            }
                        } catch (JSONException e) {
                            startActivity(new Intent(view.getContext(), ServerErrorActivity.class));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        startActivity(new Intent(view.getContext(), ServerErrorActivity.class));
                    }
                });

        queue.add(jsonObjectRequest);
    }

    /**
     * Changes the cardCameraContainer side to match the aspect ratio of the camera source.
     *
     * @param cameraSize          contains the size of the camera feed
     * @param cardCameraContainer contains the CardView component holding the camera feed
     */
    private void setCardCameraContainerSize(Size cameraSize, CardView cardCameraContainer) {
        int cameraHeight = cameraSize.getWidth(); // Reading Portrait Resolution
        int cameraWidth = cameraSize.getHeight(); // Reading Portrait Resolution
        float cameraAspect = (float) cameraWidth / cameraHeight;

        ViewGroup.LayoutParams cardLayoutParams = cardCameraContainer.getLayoutParams();
        int maxHeight = cardCameraContainer.getHeight();
        int maxWidth = cardCameraContainer.getWidth();

        int adjustedHeight = (int) (maxWidth / cameraAspect);
        int adjustedWidth = (int) (maxHeight * cameraAspect);

        if (adjustedHeight <= maxHeight) {
            cardLayoutParams.height = adjustedHeight;
            cardLayoutParams.width = maxWidth;
        } else {
            cardLayoutParams.height = maxHeight;
            cardLayoutParams.width = adjustedWidth;
        }

        cardCameraContainer.setLayoutParams(cardLayoutParams);
    }

}

