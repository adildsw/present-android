package com.adildsw.present;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class HostControlsFragment extends Fragment {
    private View view;

    private TextView tvCourseCode, tvCourseName, tvFacultyCode, tvDate;
    private ImageView imgQR;

    DataUtils dUtils;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_host_controls, container, false);

        tvCourseCode = view.findViewById(R.id.tvCourseCode);
        tvCourseName = view.findViewById(R.id.tvCourseName);
        tvFacultyCode = view.findViewById(R.id.tvFacultyCode);
        tvDate = view.findViewById(R.id.tvDate);

        imgQR = view.findViewById(R.id.imgQR);

        dUtils = new DataUtils(view.getContext());

        final Handler handlerRequestHostControls = new Handler();
        final Runnable runnableRequestHostControls = new Runnable() {
            @Override
            public void run() {
                requestHostControls();
                handlerRequestHostControls.postDelayed(this, 5000);
            }
        };
        handlerRequestHostControls.post(runnableRequestHostControls);

        return view;
    }

    /**
     * Requests host controls from server.
     */
    private void requestHostControls() {
        HashMap<String, String> params = new HashMap<>();
        params.put("device_uid", dUtils.readDataFromSP("deviceUID"));

        String url = dUtils.readDataFromSP("serverAddress") + "/request_host_controls";
        RequestQueue queue = Volley.newRequestQueue(view.getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("S0")) {
                                setHostControls(response);
                            }
                            else {
                                resetSessionInfo();
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
     * Generates QR Code image and loads it on the ImageView component.
     *
     * @param qrCodeID  contains string to be encoded in the QR Code
     */
    private void generateQR(String qrCodeID) {
        int side = 1024;
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(qrCodeID, BarcodeFormat.QR_CODE,
                    side, side);
            Bitmap bmpQR = new BarcodeEncoder().createBitmap(bitMatrix);
            imgQR.setImageBitmap(bmpQR);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets session information and unloads QRCode to N/A.
     */
    public void resetSessionInfo() {
        tvCourseCode.setText("N/A");
        tvCourseName.setText("N/A");
        tvFacultyCode.setText("N/A");
        tvDate.setText("N/A");

        imgQR.setImageResource(R.drawable.qrhost_img_dark);
    }

    /**
     * Sets session information and loads QRCode as received from sessionRes.
     *
     * @param sessionRes        contains session information
     * @throws JSONException    throws JSONException
     */
    public void setHostControls(JSONObject sessionRes) throws JSONException {
        tvCourseCode.setText(sessionRes.getString("course_code"));
        tvCourseName.setText(sessionRes.getString("course_name"));
        tvFacultyCode.setText(sessionRes.getString("op_faculty_code"));
        tvDate.setText(sessionRes.getString("date"));

        generateQR(sessionRes.getString("qrcode_id"));
    }
}
