package com.example.smsweekly;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class DeleteSentSms extends AppCompatActivity {

    boolean flagDeletedMessages = false;
    String app;

    SharedPreferences preference;
    SharedPreferences.Editor editor;

    @Override
    protected void onResume() {
        final String mySmsApp = getApplicationContext().getPackageName();
        final EditText editText = (EditText) findViewById(R.id.editTextSmsSpeed);

        if (Telephony.Sms.getDefaultSmsPackage(getApplicationContext()).equals(mySmsApp))
        {
            deleteSMS(getApplicationContext(), editText.getText().toString());
            flagDeletedMessages = true;
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, app);
            startActivity(intent);
        }
        if (flagDeletedMessages) {
            finish();
        }
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_sms);

        final EditText editText = (EditText) findViewById(R.id.editTextSmsSpeed);
        final Button buttonDelete = (Button) findViewById(R.id.buttonDelete);

        preference = getApplicationContext().getSharedPreferences("muneebFile",0);//private mode
        editor = preference.edit();

        final String text = preference.getString("textMessageValue","ASSALAM O ALAIKUM. ");// WA RAHMAT ULLAH WA BARAKAT ULLAH.

        editText.setText(text);
        final String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(getApplicationContext());
        app = defaultSmsApp;
        final String mySmsApp = getApplicationContext().getPackageName();

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                if (Telephony.Sms.getDefaultSmsPackage(getApplicationContext()).equals(mySmsApp))
                {
                    deleteSMS(getApplicationContext(), editText.getText().toString());
                    flagDeletedMessages = true;
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
                    startActivity(intent);
                }
                else
                {
                    showMessageOKCancel("Press YES 3 TIMES.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, mySmsApp);
                            startActivity(intent);
                        }
                    });
                }

                //finish();
            }
        });


    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(message);
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton("YES", okListener);
        adb.setNegativeButton("NO", null);
        adb.show();
    }
    public void deleteSMS(final Context context, final String message) {
                try {

                    Util.log(preference, editor, "Deleting SMS from inbox");

                    Uri uriSms = Uri.parse("content://sms");
                    //Uri uriSms = Uri.parse("content://sms/inbox");
                    Cursor c = getApplicationContext().getContentResolver().query(uriSms,
                            new String[] { "_id", "thread_id", "address",
                                    "person", "date", "body" }, null, null, null);

                    int i = 0;
                    if (c != null && c.moveToFirst()) {
                        do {
                            long id = c.getLong(0);
                            long threadId = c.getLong(1);
                            String address = c.getString(2);
                            String body = c.getString(5);

                            if (body.contains(message)) {
                                i++;
                                Util.log(preference, editor,  i +") delete SMS with id: " + threadId);
                                context.getContentResolver().delete(
                                        Uri.parse("content://sms/" + id), null, null);
                            }
                        } while (c.moveToNext());
                    }
                } catch (Exception e) {
                    Util.log(preference, editor, "Could not delete SMS from inbox: " + e.getMessage());
                }
            }

}
