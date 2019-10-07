package com.example.smsweekly;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Logs extends AppCompatActivity {

    TextView textViewLogs;
    Button buttonSeeMoreLogs;
    SharedPreferences preference;
    SharedPreferences.Editor editor;
    int logsCounter = 0;

    void initializeVars()
    {
        textViewLogs = findViewById(R.id.textViewLogs);

        buttonSeeMoreLogs = findViewById(R.id.buttonSeeMoreLogs);

        preference = getApplicationContext().getSharedPreferences(Util.MUNEEB_FILE ,Util.PRIVATE_MODE);// 0 = private mode
        editor = preference.edit();

        logsCounter = preference.getInt(Util.LOG_COUNTER, Util.LOG_COUNTER_DEFAULT);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logs);

        initializeVars();
        setListener();
        work();
    }

    private void setListener() {
        buttonSeeMoreLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String token = "";
                String history = "";
                int i = 0;

                while  (true)
                {
                    i++;

                    token = preference.getString(Util.LOG +logsCounter, "null");
                    logsCounter--;

                    if (i == 100)
                    {
                        logsCounter++;
                        break;
                    }
                    else if (token.equals("null"))
                    {
                        logsCounter++;
                        break;
                    }
                    else
                    {
                        history += token + "\n";
                    }
                }

                textViewLogs . append("\n" + history);
            }
        });
    }

    void work() {
        buttonSeeMoreLogs.performClick();
    }
}
