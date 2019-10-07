package com.example.smsweekly;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public  class History  extends AppCompatActivity {

    SharedPreferences preference;
    SharedPreferences.Editor editor;

    TextView textViewSmsHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        preference = getApplicationContext().getSharedPreferences(Util.MUNEEB_FILE ,Util.PRIVATE_MODE);// 0 = private mode
        editor = preference.edit();

        textViewSmsHistory = (TextView) findViewById(R.id.textViewSmsHistory);


        String token = "";
        String history = "";
        int counter = 0;

        while  (true)
        {
            counter++;
            token = preference.getString(Util.SMS_TOTAL_IN_A_DAY +counter, "null");
            if (token.equals("null"))
            {
                    break;
            }
            else
            {
                token += ", sms sent: " + preference.getInt(token, 0) + "\n";
                history += token;
            }
        }


        history += "\n================\n================\n================\n================\n\nNOT SENT:\n";

        counter = 0;
        while  (true)
        {
            counter++;
            token = preference.getString(Util.NOT_SENT_SUMMARY+counter, "null");
            if (token.equals("null"))
            {
                break;
            }
            else
            {
                history += token + "\n\n";
            }
        }

        history += "\n================\n================\n================\n================\n\nSENT:\n";
        counter = 0;

        while  (true)
        {
            counter++;
            token = preference.getString(Util.SENT_SUMMARY+counter, "null");
            if (token.equals("null"))
            {
                break;
            }
            else
            {
                history += token + "\n----------------\n----------------";
            }
        }

        textViewSmsHistory.setText(history);
    }
}
