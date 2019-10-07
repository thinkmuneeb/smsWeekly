package com.example.smsweekly;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class SeeSelectedContacts extends AppCompatActivity {

    TextView textViewSeeSelectedContacts;
    TextView textViewSeeSelectedContactsTotal;
    SharedPreferences preferences;
    Editor editor;
    String stringDataForSms;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.see_selected_contacts);

        textViewSeeSelectedContacts = (TextView) findViewById(R.id.textViewSeeSelectedContacts);
        textViewSeeSelectedContactsTotal = (TextView) findViewById(R.id.textViewSeeSelectedContactsTotal);
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("muneebFile",0);//private mode
        Editor editor = preferences.edit();

        stringDataForSms = preferences.getString("dataForSmsValue","Send sms to see selected contacts.");
        int selectedContacts = preferences.getInt("LinesInDataForSmsValue", 0);

        textViewSeeSelectedContactsTotal.setText("Selected contacts: " + selectedContacts);
        textViewSeeSelectedContacts.setText(stringDataForSms);
    }
}
