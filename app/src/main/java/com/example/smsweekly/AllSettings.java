package com.example.smsweekly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class AllSettings extends AppCompatActivity {
    Button buttonHistoryActivity;
    Button buttonDeleteActivity;
    Button buttonSelectContactsActivity;
    Button buttonSettingsActivity;
    Button buttonInfoActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_settings);

        buttonHistoryActivity = (Button) findViewById(R.id.buttonHistoryActivity);
        buttonDeleteActivity = (Button) findViewById(R.id.buttonDeleteSmsActivity);
        buttonSelectContactsActivity = (Button) findViewById(R.id.buttonSelectContactsActivity);
        buttonSettingsActivity = (Button) findViewById(R.id.buttonSettingsActivity);
        buttonInfoActivity = (Button) findViewById(R.id.buttonInfoActivity);

        buttonDeleteActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AllSettings.this, DeleteSentSms.class));
            }
        });

        buttonSelectContactsActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AllSettings.this, SelectContactsActivity.class));
            }
        });

        buttonSettingsActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AllSettings.this, Settings.class));
            }
        });

        buttonHistoryActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AllSettings.this, History.class));
            }
        });

        buttonHistoryActivity.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                startActivity(new Intent(AllSettings.this, Logs.class));
                return false;
            }
        });

        buttonInfoActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AllSettings.this, AppInfo.class));
            }
        });
    }
}
