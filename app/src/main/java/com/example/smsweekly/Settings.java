package com.example.smsweekly;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class Settings extends AppCompatActivity {
    EditText editText;
    EditText editText2;
    EditText editText3;
    EditText editText4;
    CheckBox checkBoxIsTesting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        editText = (EditText) findViewById(R.id.editTextSmsSpeed);
        editText2 = (EditText) findViewById(R.id.editTextSmsSpeed2);
        editText3 = (EditText) findViewById(R.id.editTextSmsSpeed3);
        editText4 = (EditText) findViewById(R.id.editTextSmsSpeed4);

        checkBoxIsTesting = (CheckBox) findViewById(R.id.checkBoxIsTesting);

        final Button buttonSave = (Button) findViewById(R.id.buttonSave);

        SharedPreferences preference = getApplicationContext().getSharedPreferences("muneebFile",0);//private mode
        final SharedPreferences.Editor editor = preference.edit();

        long speed = preference.getLong("smsSpeedValue",5);
        editText.setText(speed+"");

        int limit = preference.getInt("smsLimitValue",2900);
        editText2.setText(limit+"");

        int daySecValue = preference.getInt("daySecValue",86400);
        editText3.setText(daySecValue+"");

        int sendInNextHours = preference.getInt("sendInNextHoursValue",0);
        editText4.setText(sendInNextHours+"");

        if (preference.getInt(Util.IN_TESTING, 0) == 1)
            checkBoxIsTesting.setChecked(true);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                try {
                    long m = Long.parseLong(editText.getText().toString());
                    int n = Integer.parseInt(editText2.getText().toString());
                    int p = Integer.parseInt(editText3.getText().toString());
                    int l = Integer.parseInt(editText4.getText().toString());

                    //Toast.makeText(getApplicationContext(),""+m,Toast.LENGTH_SHORT).show();
                    if (m < 0)
                    {
                        m = 1;
                    }//minimum delay per sms is 0.001 second

                    if (n <= 0)
                        n = 1; //minimum limit to send sms is 1

                    if (p <= 0)
                        p = 10;//send remaining sms in next 86400(default) seconds

                    if (l < 0)
                        l = 0;//send in next X hours after pressing button send.

                    if (checkBoxIsTesting.isChecked())
                        editor.putInt(Util.IN_TESTING, 1);
                    else
                        editor.putInt(Util.IN_TESTING, 0);

                    editor.putInt("smsLimitValue", n);

                    editor.putLong("smsSpeedValue", m);
                    editor.putInt("daySecValue", p);
                    editor.putInt("sendInNextHoursValue", l);
                    editor.apply();
                }
                catch (Exception e)
                {

                }
                finish();
            }
        });


    }
}
