package com.example.smsweekly;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Contact extends Activity {
    Button buttonCall;
    Button buttonEmail;
    Button buttonSms;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact);

        setVars();
        doWork();
    }

    private void doWork() {
        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               composeCall(getString(R.string.MuneebPhoneNumber));
            }
        });
        buttonSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeSms(getString(R.string.MuneebPhoneNumber),"");
            }
        });
        buttonEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeEmail(new String[]{getString(R.string.email)},"BULK SMS APP");
            }
        });

    }

    private void setVars() {
        buttonCall = (Button) findViewById(R.id.buttonCall);
        buttonEmail = (Button) findViewById(R.id.buttonEmail);
        buttonSms = (Button) findViewById(R.id.buttonSms);
    }

    void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    void composeSms(String phoneNum, String sms_body) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNum, null)).putExtra("sms_body", sms_body));
    }

    void composeCall(String phoneNum) {
        String uri = "tel:" + phoneNum ;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

}
