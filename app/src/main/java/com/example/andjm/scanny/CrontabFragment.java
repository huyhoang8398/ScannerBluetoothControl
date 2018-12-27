package com.example.andjm.scanny;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CrontabFragment extends DialogFragment {
    private EditText textDPI;
    private TextView textTime;
    private Button changeDPI;
    private Button changeTime;
    private Button button1, button2, button3, button4, button5;
    private Button date_time_set;
    private TextView noti_changed_DPI;
    private TextView noti_changed_TIME;
    private boolean isAdmin;


    public static CrontabFragment newInstance(String data) {
        CrontabFragment dialog = new CrontabFragment();
        Bundle args = new Bundle();
        args.putString("data", data);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        String usertype = getArguments().getString("usertype");
        if(usertype.equals("admin")) isAdmin = true;
        else isAdmin = false;
        return inflater.inflate(R.layout.crontab_fragment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        String data= getArguments().getString("data", "");
        textDPI    = view.findViewById(R.id.textDPI);
        textTime   = view.findViewById(R.id.textTime);
        changeDPI  = view.findViewById(R.id.changeDPI);
        changeTime = view.findViewById(R.id.changeTime);

        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);
        button4 = view.findViewById(R.id.button4);
        button5 = view.findViewById(R.id.button5);

        date_time_set    = view.findViewById(R.id.date_time_set);
        noti_changed_DPI = view.findViewById(R.id.noti_changed_DPI);
        noti_changed_TIME= view.findViewById(R.id.noti_changed_TIME);

        if(isAdmin){
            textDPI.setVisibility(View.VISIBLE);
            changeDPI.setVisibility(View.VISIBLE);
        }else {
            textDPI.setVisibility(View.INVISIBLE);
            changeDPI.setVisibility(View.INVISIBLE);
        }

        changeDPI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //textDPI.setText("0");
                if(textDPI.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(),"DPI value can't be blank!", Toast.LENGTH_SHORT).show();
                }else{
                    int DPI = Integer.parseInt(textDPI.getText().toString());
                    if (checkRangeDPI(DPI)){
                        String update_dpi = textDPI.getText().toString();
                        ((CentralActivity) getActivity()).sendMessage("DPI " + update_dpi);
                        noti_changed_DPI.setText("DPI updated to "+update_dpi);
                        textDPI.setText("");
                        Toast.makeText(getActivity(),"DPI updated to "+update_dpi, Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getActivity(),"DPI must be between 100 and 500", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CentralActivity) getActivity()).sendMessage("DPI " + "100");
                noti_changed_DPI.setText("DPI updated to "+"100");
                Toast.makeText(getActivity(),"DPI updated to "+"100", Toast.LENGTH_SHORT).show();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CentralActivity) getActivity()).sendMessage("DPI " + "200");
                noti_changed_DPI.setText("DPI updated to "+"200");
                Toast.makeText(getActivity(),"DPI updated to "+"200", Toast.LENGTH_SHORT).show();
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CentralActivity) getActivity()).sendMessage("DPI " + "300");
                noti_changed_DPI.setText("DPI updated to "+"300");
                Toast.makeText(getActivity(),"DPI updated to "+"300", Toast.LENGTH_SHORT).show();
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CentralActivity) getActivity()).sendMessage("DPI " + "400");
                noti_changed_DPI.setText("DPI updated to "+"400");
                Toast.makeText(getActivity(),"DPI updated to "+"400", Toast.LENGTH_SHORT).show();
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CentralActivity) getActivity()).sendMessage("DPI " + "500");
                noti_changed_DPI.setText("DPI updated to "+"500");
                Toast.makeText(getActivity(),"DPI updated to "+"500", Toast.LENGTH_SHORT).show();
            }
        });

        textTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*String update_time = textTime.getText().toString();

                ((CentralActivity) getActivity()).sendMessage("Time " + update_time);
                noti_changed_TIME.setText("Time updated to "+update_time);
                textTime.setText("");
                Toast.makeText(getActivity(),"Time updated to "+update_time, Toast.LENGTH_SHORT).show();*/

                final View dialogview = View.inflate(getActivity(), R.layout.date_time_picker,null);
                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();

                dialogview.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View view) {
                        DatePicker datePicker = dialogview.findViewById(R.id.date_picker);
                        TimePicker timePicker = dialogview.findViewById(R.id.time_picker);

                            Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                                    datePicker.getMonth(),
                                    datePicker.getDayOfMonth(),
                                    timePicker.getHour(),
                                    timePicker.getMinute());

                            Long time = calendar.getTimeInMillis();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
                            alertDialog.dismiss();
                            textTime.setText(sdf.format(time));
                    }
                });
                alertDialog.setView(dialogview);
                alertDialog.show();
            }
        });

        changeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String update_time = textTime.getText().toString();
                if (update_time.equals(""))
                Toast.makeText(getActivity(), "Pick a time please!", Toast.LENGTH_SHORT).show();
                else {
                    ((CentralActivity) getActivity()).sendMessage("Time " + update_time);
                    noti_changed_TIME.setText("Time updated to " + update_time);
                    textTime.setText("");
                }
            }
        });
    }

    private boolean checkRangeDPI(int value){
        if(100 <= value && value <= 500) return true;
        return false;
    }


}
