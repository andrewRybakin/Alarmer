package com.mercury.alarmer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TimePickerDialog timePickerDialog;
    private DatePickerDialog datePickerDialog;
    private Button date, time, save;
    private AlarmController alarmController;
    private EditText title, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
        date = (Button) findViewById(R.id.date_button);
        time = (Button) findViewById(R.id.time_button);
        save = (Button) findViewById(R.id.save_button);
        alarmController = AlarmController.getInstance(this);
        if (alarmController.isTimeExpired())
            alarmController.reset();
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                alarmController.setDate(year, monthOfYear, dayOfMonth);
                date.setText(alarmController.getDate());
            }
        }, alarmController.getYear(), alarmController.getMonth(), alarmController.getDay());
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                alarmController.setTime(hourOfDay, minute);
                time.setText(alarmController.getTime());
            }
        }, alarmController.getHour(), alarmController.getMinute(), true);
        date.setOnClickListener(this);
        time.setOnClickListener(this);
        save.setOnClickListener(this);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                alarmController.setTitle(s.toString());
            }
        });
        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                alarmController.setDescription(s.toString());
            }
        });
        date.setText(alarmController.getDate());
        time.setText(alarmController.getTime());
        title.setText(alarmController.getTitle());
        description.setText(alarmController.getDescription());
        if (alarmController.isAlarmSet())
            save.setText(R.string.update);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.date_button:
                datePickerDialog.show();
                break;
            case R.id.time_button:
                timePickerDialog.show();
                break;
            case R.id.save_button:
                if (isAllValid()) {
                    alarmController.setAlarm(this);
                    save.setText(R.string.update);
                } else {
                    Toast.makeText(this, "Something wrong!", Toast.LENGTH_LONG).show();
                }
        }
    }

    private boolean isAllValid() {
        return !title.getText().toString().equals("") && !description.getText().toString().equals("");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}



