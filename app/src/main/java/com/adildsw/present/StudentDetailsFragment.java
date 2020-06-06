package com.adildsw.present;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class StudentDetailsFragment extends Fragment {
    private TextView tvOrgName, tvServerAddress;
    private TextView tvStudentName, tvStudentRoll, tvDepartment, tvSection, tvStartYear, tvGradYear;
    private DataUtils dUtils;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_details, container, false);

        tvOrgName = view.findViewById(R.id.tvOrgName);
        tvServerAddress = view.findViewById(R.id.tvServerAddress);
        tvStudentName = view.findViewById(R.id.tvStudentName);
        tvStudentRoll = view.findViewById(R.id.tvStudentRoll);
        tvDepartment = view.findViewById(R.id.tvDepartment);
        tvSection = view.findViewById(R.id.tvSection);
        tvStartYear = view.findViewById(R.id.tvStartYear);
        tvGradYear = view.findViewById(R.id.tvGradYear);
        Button btnExitServer = view.findViewById(R.id.btnExitServer);

        dUtils = new DataUtils(Objects.requireNonNull(getActivity()));

        loadDetails();

        btnExitServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitServer(v);
            }
        });

        return view;
    }

    /**
     * Loading details of the organization and student on the UI elements.
     */
    private void loadDetails() {
        tvOrgName.setText(dUtils.readDataFromSP("orgName"));
        tvServerAddress.setText(dUtils.readDataFromSP("serverAddress"));
        tvStudentName.setText(dUtils.readDataFromSP("studentName"));
        tvStudentRoll.setText(dUtils.readDataFromSP("studentRoll"));
        tvDepartment.setText(dUtils.readDataFromSP("deptName"));
        tvSection.setText(dUtils.readDataFromSP("section"));
        tvStartYear.setText(dUtils.readDataFromSP("startYear"));
        tvGradYear.setText(dUtils.readDataFromSP("gradYear"));
    }

    /**
     * Exits server by clearing SharedPreferences and redirecting to LoaderActivity.
     *
     * @param view  view object for getting the context
     */
    private void exitServer(final View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
        alertDialogBuilder.setTitle("Confirm Action");
        alertDialogBuilder.setMessage("Are you sure you want to exit this server?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dUtils.clearSP();
                startActivity(new Intent(view.getContext(), LoaderActivity.class));
            }
        });
        alertDialogBuilder.setNegativeButton("No", null);
        alertDialogBuilder.show();
    }
}
