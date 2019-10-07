package com.example.smsweekly;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 1;//can be any number to identify request

    BroadcastReceiver broadcastReceiverSmsArrived;

    Button buttonSettingsActivity;
    Button buttonStartService;
    Button buttonStopService;
    Button buttonContact;

    EditText smsText;
    TextView textViewSelectedContacts;

    TextView smsAlarmInfo;

    SharedPreferences preference;
    Editor editor;


    @Override
    protected void onResume() {
        int profile = preference.getInt(Util.SMS_PROFILE,0);
        String token = preference.getString("tokenValue","A");
        if (profile == 0)
            textViewSelectedContacts.setText("All contacts selected.");

        else if(profile == 1)
            textViewSelectedContacts.setText("All Contacts selected that start with '" + token+"'.");

        else if(profile == 2)
            textViewSelectedContacts.setText("Contacts selected that start without '" + token+"'.");

        else if(profile == 3)
            textViewSelectedContacts.setText("Contacts from file" + token);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String mySmsApp = getApplicationContext().getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(getApplicationContext()).equals(mySmsApp)) {

                    showMessageOKCancel("Press YES 2 TIMES.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, mySmsApp);
                            startActivity(intent);
                        }
                    });
            }
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiverSmsArrived);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ContactsUtility.testCode(getApplicationContext());
        //Util.show(ContactsUtility.getGroupsSummary(getApplicationContext()));

        preference = getApplicationContext().getSharedPreferences("muneebFile",0);//private mode
        editor = preference.edit();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

// The id of the channel.
            String id = "Channel_Id_Muneeb_Khan";

// The user-visible id of the channel.
            CharSequence name = getString(R.string.channel_name);

// The user-visible description of the channel.
            String description = getString(R.string.channel_description);

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel mChannel = new NotificationChannel(id, name, importance);

// Configure the notification channel.
            mChannel.setDescription(description);

            mNotificationManager.createNotificationChannel(mChannel);
        }

        buttonStartService = (Button)findViewById(R.id.buttonStartService);
        buttonStopService = (Button)findViewById(R.id.buttonStopService);
        buttonSettingsActivity = (Button) findViewById(R.id.buttonSettingsActivity);
        buttonContact = (Button) findViewById(R.id.buttonContact);

        buttonContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Contact.class));
            }
        });
        smsAlarmInfo = (TextView) findViewById(R.id.smsAlarmInfo);

        smsText = (EditText) findViewById(R.id.smsText);

        textViewSelectedContacts = (TextView) findViewById(R.id.textViewSelectedContacts);

        int profile = preference.getInt(Util.SMS_PROFILE,0);
        String token = preference.getString("tokenValue","A");
        if (profile == 0)
            textViewSelectedContacts.setText("All contacts selected.");

        else if(profile == 1)
            textViewSelectedContacts.setText("All Contacts selected that start with '" + token+"'.");

        else if(profile == 2)
            textViewSelectedContacts.setText("Contacts selected that start without '" + token+"'.");

        else if(profile == 3)
            textViewSelectedContacts.setText("Contacts from file" + token);

          //Setting up the interface
        smsText.setText(preference.getString("textMessageValue", "ASSALAM O ALAIKUM. "));// WA RAHMAT ULLAH WA BARAKAT ULLAH.

        if (preference.getInt("alarmValue", 0) == 1)
            smsAlarmInfo.setText(preference.getString("alarmInfoValue", ""));
        else
            smsAlarmInfo.setText("");


        if(preference.getInt("alarmValue",0) == 1)
        {
            buttonStartService.setVisibility(View.INVISIBLE);
            buttonStopService.setVisibility(View.VISIBLE);
        }

        if (preference.getInt("serviceValue",0) == 1)
        {
            buttonStartService.setVisibility(View.INVISIBLE);
            buttonStopService.setVisibility(View.VISIBLE);
        }

        buttonSettingsActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AllSettings.class));
            }
        });

        buttonStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean p1 = (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED);
                boolean p2 = (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
                boolean p3 = (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                boolean p4 = (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED);

                if (p1 && p2 && p3 && p4) {

                    String expiryDate = preference.getString("bulkSmsAppExpiryDate", "null");
                    boolean isExpired = greaterThan(getDateTimeInString(), expiryDate);

                    //if (expiryDate.equals("null") || isExpired)
                    if (expiryDate.equals("null") || isExpired)
                    {
                        if (isExpired)
                        {
                            editor.putString("bulkSmsAppExpiryDate", "null");
                            editor.apply();
                        }

                        if(preference.getInt(Util.FlagActivatedOnce, Util.FlagActivatedOnceDefault) == 0)
                        {
                            Toast.makeText(getApplicationContext(), "This app will be activated in 10 seconds Insha Allah.", Toast.LENGTH_LONG).show();
                            SmsManager.getDefault().sendTextMessage("+923014440289", null, "demo", null , null);
                            editor.putInt(Util.FlagActivatedOnce, 1);
                            editor.apply();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Contact developer to activate app.", Toast.LENGTH_LONG).show();
                            SmsManager.getDefault().sendTextMessage("+923014440289", null, "demo", null , null);
                        }
                    }
                    else
                    {
                        int todaySms = preference.getInt(geDate(), 0);
                        int limitOfSms = preference.getInt("smsLimitValue",2900);
                        if (todaySms < limitOfSms) {
                            int seconds = 60 * 60 * preference.getInt("sendInNextHoursValue", 0);

                            if (seconds == 0)
                                seconds = 1; //start in minimum 1 seconds

                            Alarm a = new Alarm();
                            a.setAlarm(getApplicationContext(), seconds);

                            editor.putString("textMessageValue", smsText.getText().toString());
                            editor.apply();

                            buttonStopService.setVisibility(View.VISIBLE);
                            buttonStartService.setVisibility(View.INVISIBLE);

                            String smsAlarm;

                            if (seconds / 86400 >= 1)
                                smsAlarm = ("SMS sending will start in " + (seconds / 86400) + " days.\nFrom:" + getDateTimeInString());
                            else if (seconds / (60 * 60) >= 1)
                                smsAlarm = ("SMS sending will start in " + (seconds / 3600) + " hours.\nFrom:" + getDateTimeInString());
                            else if (seconds / (60) >= 1)
                                smsAlarm = ("SMS sending will start in " + (seconds / 60) + " minutes.\nFrom:" + getDateTimeInString());
                            else
                                smsAlarm = "SEE NOTIFICATIONS.";

                            smsAlarmInfo.setText(smsAlarm);
                            Toast.makeText(getApplicationContext(), smsAlarm, Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(getApplicationContext(), "Sms limit reached to maximum.", Toast.LENGTH_LONG).show();
                    }
                }
                else
                    {
                    requestStoragePermission();
                }

            }
        });

        buttonStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonStartService.setVisibility(View.VISIBLE);
                buttonStopService.setVisibility(View.INVISIBLE);

                SharedPreferences preferenceService = getApplicationContext().getSharedPreferences("muneebFile",0);//private mode
                Editor editor = preferenceService.edit();

                int serviceFlag = preferenceService.getInt("serviceValue",0);
                if (serviceFlag == 1)
                {
                    Toast.makeText(getApplicationContext(), "Sms stopped.", Toast.LENGTH_SHORT).show();
                    editor.putInt("serviceValue",0);
                    editor.apply();
                }
                else
                    Toast.makeText(getApplicationContext(), "Sms already stopped", Toast.LENGTH_SHORT).show();

                smsAlarmInfo.setText("");

                if (preference.getInt("alarmValue", 0) == 1)
                {
                    Alarm a = new Alarm();
                    a.cancelAlarm(getApplicationContext());

                    MainActivity.this.editor.putString("alarmInfoValue", "");
                    MainActivity.this.editor.apply();
                }
            }
        });

        broadcastReceiverSmsArrived = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                showMessageOKCancel("App is activated.", null);
                buttonStartService.performClick();
            }
        };
        registerReceiver(broadcastReceiverSmsArrived, new IntentFilter(Util.APP_REGISTERED));
    }

    private String geDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        return formatter.format(date);
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to save SMS delivery report.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS}, STORAGE_PERMISSION_CODE);
        }
    }

    void NotEligibleFileMake()
    {
        String summary, phonesNotEligible = "", phonesNotSuccessSms, phonesSuccessSms, phone, name;
        int phoneNotEligibleTotal = 0;
        String start = getDateTimeInString(), end;

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},null,null,null);
        int NAME = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int NUMBER = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        int i = 0;
        int total = cursor.getCount();

        while(cursor.moveToNext())
        {
            Util.log(preference, editor,  i+"/"+total);
            i++;
            phone = cursor.getString(NUMBER);
            name = cursor.getString(NAME);
            if(!isEligible(phone))
            {
                phonesNotEligible += phone + " " + name + "\n";
                phoneNotEligibleTotal++;
            }
        }
        phonesNotEligible = "total phones not eligible: " + phoneNotEligibleTotal + "/" + total + "\n" +phonesNotEligible;
        generateNoteOnSD("NotEligible " + getDateTimeInString(), phonesNotEligible);

    }

    void buttonSettingActivityWork()
    {

    }

    void DuplicatesFileMake()
    {
        String summary, phonesNotEligible = "", phonesNotSuccessSms, phonesSuccessSms, phone, name;
        int phoneNotEligibleTotal = 0;
        String start = getDateTimeInString(), end;

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},null,null,null);
        int NAME = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int NUMBER = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        int i = 0;
        int total = cursor.getCount();

        Hashtable<String, Boolean> ht = new Hashtable<>();

        while(cursor.moveToNext())
        {
            Util.log(preference, editor,  i+"/"+total);
            i++;
            phone = cursor.getString(NUMBER);
            name = cursor.getString(NAME);
            if(ht.containsKey(phone))
            {
                phonesNotEligible += phone + " " + name + "\n";
                phoneNotEligibleTotal++;
            }
            else
                ht.put(phone, true);
        }
        phonesNotEligible = "total phones duplicates: " + phoneNotEligibleTotal + "/" + total + "\n" +phonesNotEligible;
        generateNoteOnSD("Duplicates " + getDateTimeInString(), phonesNotEligible);

    }

    void EligiblePhonesFileMake()
    {
        String summary, phonesNotEligible = "", phonesNotSuccessSms, phonesSuccessSms, phone, name;
        int phoneNotEligibleTotal = 0;
        String start = getDateTimeInString(), end;

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int NAME = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int NUMBER = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        int i = 0;
        int total = cursor.getCount();
        Hashtable<String, Boolean> ht = new Hashtable<>();

        while(cursor.moveToNext())
        {
            Util.log(preference, editor,  i+"/"+total);
            i++;
            phone = cursor.getString(NUMBER);
            name = cursor.getString(NAME);
            if(isEligible(phone))
            {
                if(!ht.containsKey(phone))
                {
                    phonesNotEligible += i + " " + phone + " " + name + "\n";
                    phoneNotEligibleTotal++;
                }
                else
                    ht.put(phone,true);
            }
        }
        phonesNotEligible = "total phones eligible: " + phoneNotEligibleTotal + "/" + total + "\n" +phonesNotEligible;
        generateNoteOnSD("YesEligible " + getDateTimeInString(), phonesNotEligible);

    }

    public void generateNoteOnSD(String FileName, String sBody) {
        String sFileName = FileName + ".txt";
        //Environment.getExternalStorageState()
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "cMuneeb");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            //Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
            Util.log(preference, editor, "Saved");

        } catch (IOException e) {
            //Toast.makeText(context, "Not Saved, Enable Storage Permission!", Toast.LENGTH_SHORT).show();
            Util.log(preference, this.editor, "Not Saved");
            e.printStackTrace();
        }
    }

    private String getDateTimeInString()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
        Date date = new Date();
        return formatter.format(date);
    }

    private boolean isEligible(String p)//phone
    {
        p = p.replaceAll("\\s","");
        if(p.length() >= 10 && p.length() <= 14)
        {
            //later dynamic filter
            if(p.charAt(0) == '0' && p.charAt(1) =='3')
                return true;
            else if(p.charAt(0) == '0' && p.charAt(1) =='0' && p.charAt(2) == '9' && p.charAt(3) =='2'&& p.charAt(4) == '3')
                return true;
            else if(p.charAt(0) == '+' && p.charAt(1) =='9' && p.charAt(2) == '2' && p.charAt(3) =='3')
                return true;
        }

        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                final Button buttonStartService = (Button)findViewById(R.id.buttonStartService);
                buttonStartService.performClick();
                //Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    boolean greaterThan(String dateA, String dateB)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
        Date date = new Date();

        Calendar calendarDateA = Calendar.getInstance();
        Calendar calendarDateB = Calendar.getInstance();

        try{
            calendarDateA.setTime(formatter.parse(dateA));
            calendarDateB.setTime(formatter.parse(dateB));
        }

        catch(ParseException e){
            e.printStackTrace();
        }

        int difference = calendarDateA.compareTo(calendarDateB);

        if (difference >= 0)
            return true;
        return false;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("YES", okListener)
                .setNegativeButton("NO", null)
                .create()
                .show();
    }
}
/*
SharedPreferences preferenceService = context.getSharedPreferences("serviceValue",0);//private mode
SharedPreferences.Editor editor = preferenceService.edit();
editor.putInt("serviceValue",1);//so that service can not start and alarm can not set again
editor.apply();
*/
 /* final SharedPreferences preference = getApplicationContext().getSharedPreferences("iValue",0);//private mode
        final SharedPreferences.Editor editor = preference.edit();
        int i = 0;
        editor.putInt("iValue",i);
        editor.apply(); */

 /*
 //final TextView selectedContactsTextView = (TextView) findViewById(R.id.selectedContactsTextView);

        //TextView textViewSmsSetting = (TextView) findViewById(R.id.textViewSmsSetting);

       textViewSmsSetting.setText("SMS SETTINGS:\nDELEAY IN EACH SMS = " + p.getInt("smsSpeedValue",5) + " SECONDS\nSMS SENDING LIMIT = " + p.getInt("smsLimitValue", 2900) + " SMS PER DAY");
        if(p.getInt("smsSendingProfileValue",0) == 0)
        {
            selectedContactsTextView.setText("SELECTED CONTACTS:\nALL CONTACTS SELECTED");
        }
        else if(p.getInt("smsSendingProfileValue",0) == 1)
        {
            selectedContactsTextView.setText("SELECTED CONTACTS:\nContacts that start with \""+ p.getString("tokenValue","A") + "\"");
        }
        else if(p.getInt("smsSendingProfileValue",0) == 2)
        {
            selectedContactsTextView.setText("SELECTED CONTACTS:\nContacts that NOT start with \""+ p.getString("tokenValue", "C") + "\"");
        }



        //i = 378 and phone = 0301...
        //file make
        //EligiblePhonesFileMake();
        //


         //handling default values and 1 st time app run
        //later set all default values for preferences

//fileMake(); //todo i enjoyed it ALHAMDULILLAH. by baba feed back.
        /*EligiblePhonesFileMake();
        NotEligibleFileMake();
        DuplicatesFileMake();*/


//Toast.makeText(getApplicationContext(),"sms app",Toast.LENGTH_SHORT).show();

        /*editor.putInt("iValue", 378);
        editor.apply();*/
// final TextView textViewSmsSetting = (TextView) findViewById(R.id.textViewSmsSetting);
//final TextView selectedContactsTextView = (TextView) findViewById(R.id.selectedContactsTextView);

 /* if(p.getInt("smsSendingProfileValue",0) == 0)
        {
           selectedContactsTextView.setText("SELECTED CONTACTS:\nALL CONTACTS SELECTED");
        }

        else if(p.getInt("smsSendingProfileValue",0) == 1)
        {
            selectedContactsTextView.setText("SELECTED CONTACTS:\nContacts that start with \""+ p.getString("tokenValue","A") + "\"");
        }
        else if(p.getInt("smsSendingProfileValue",0) == 2)
        {
            selectedContactsTextView.setText("SELECTED CONTACTS:\nContacts that NOT start with \""+ p.getString("tokenValue", "C") + "\"");
        }*/

//start: starting interface

//textViewSmsSetting.setText("SMS SETTINGS------------\nDELEAY IN EACH SMS = " + p.getInt("smsSpeedValue",5) + " SECONDS\nSMS SENDING LIMIT = " + p.getInt("smsLimitValue", 2900) + " SMS PER DAY");
//end: starting interface

//start: changes to front end
//buttonSelectContactsActivity.setVisibility(View.INVISIBLE);
//Toast.makeText(MainActivity.this, "You have already granted this permission!", Toast.LENGTH_SHORT).show();

 /*buttonCancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alarm a = new Alarm();
                a.cancelAlarm(getApplicationContext());

                editor.putString("alarmInfoValue", "");
                editor.apply();

                smsAlarmInfo.setVisibility(View.INVISIBLE);
                buttonCancelAlarm.setVisibility(View.INVISIBLE);
                buttonStartService.setVisibility(View.VISIBLE);
            }
        });*/


 /*broadcastReceiverSmsArrived = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                Util.log(preference, editor,  "onReceive sms received in MainActivity.");
                Toast.makeText(getApplicationContext(), "onReceive sms received in MainActivity.", Toast.LENGTH_LONG).show();
            }
        };
        registerReceiver(broadcastReceiverSmsArrived, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));*/


