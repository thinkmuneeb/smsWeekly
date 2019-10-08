package com.example.smsweekly;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;


public class YourService extends Service {

    void work()
    {
        try {
            setupVariables();
            setupDataStringForSms();
            sendSmsLoop();
        }
        catch (Exception e)
        {
            Util.log(preference, editor, "line 58 in exception: Error in file overall: " + e.getMessage());

            Toast.makeText(getApplicationContext(),"Error: " + e.getMessage(), Toast.LENGTH_LONG).show();

            timeToEndService();
        }

        /*Plan:
        1. Load all data tokens with no duplicates and all eligible Plus if filter then filter some more phones. (START: 13Jun19 5.00PM)(DONE 14Jun19 5.00AM)
        2. Reach to the saved token i (START: 14Jun19 5.00AM)
        3. Now Get in a loop to send sms to all and show notification per sms and save the contacts index on each iteration MUST NOTE: DO IT WITH INPUT DELAY INTERVAL. Allow user to stop from loop.
        3.1) Save summary but it will be from pending intent

        4. Permutation experiment. (Done 17June19 7.17am )
        5. In start send 200 contacts 3 times then to all(Pending 17June19 7.17am)
        */
    }

    MediaPlayer mp;

    MyPhoneStateListener mPhoneStatelistener;
    TelephonyManager mTelephonyManager;

    boolean smsSendingEnd = false;
    boolean smsSendingPendingIntentEnd = false;

    Alarm a = new Alarm();
    //120 elements
    //now we can go for 50ms or 100ms wait in countDownInterval to send sms in speed by using textmessage + arr[i%119] to textmessage + arr[i%120] // gives 0 to 119 round robin
    String[] arrPermutation = new String[]{";,.'-",";,.-'",";,'.-",";,'-.",";,-'.",";,-.'",";.,'-",";.,-'",";.',-",";.'-,",";.-',",";.-,'",";'.,-",";'.-,",";',.-",";',-.",";'-,.",";'-.,",";-.',",";-.,'",";-'.,",";-',.",";-,'.",";-,.'",",;.'-",",;.-'",",;'.-",",;'-.",",;-'.",",;-.'",",.;'-",",.;-'",",.';-",",.'-;",",.-';",",.-;'",",'.;-",",'.-;",",';.-",",';-.",",'-;.",",'-.;",",-.';",",-.;'",",-'.;",",-';.",",-;'.",",-;.'",".,;'-",".,;-'",".,';-",".,'-;",".,-';",".,-;'",".;,'-",".;,-'",".;',-",".;'-,",".;-',",".;-,'",".';,-",".';-,",".',;-",".',-;",".'-,;",".'-;,",".-;',",".-;,'",".-';,",".-',;",".-,';",".-,;'","',.;-","',.-;","',;.-","',;-.","',-;.","',-.;","'.,;-","'.,-;","'.;,-","'.;-,","'.-;,","'.-,;","';.,-","';.-,","';,.-","';,-.","';-,.","';-.,","'-.;,","'-.,;","'-;.,","'-;,.","'-,;.","'-,.;","-,.';","-,.;'","-,'.;","-,';.","-,;'.","-,;.'","-.,';","-.,;'","-.',;","-.';,","-.;',","-.;,'","-'.,;","-'.;,","-',.;","-',;.","-';,.","-';.,","-;.',","-;.,'","-;'.,","-;',.","-;,'.","-;,.'"};

    final int NOTIF_ID = 317;
    final String NOTIF_CHANNEL_ID = "Channel_Id_Muneeb_Khan";//can it be any id

    SmsManager smsManager;
    BroadcastReceiver sentStatusReceiver = null;

    int NAME;
    int PHONE;

    private final int PRIVATE_MODE = 0;


    int smsSentStatusReceivedCounter = 0;
    int smsSentAttempt = 0;

    int smsSentToday = 0;

    int i = 0;
    int profile;
    int indexInContactList;
    int totalIndexInContactList;

    int iStart;
    float x1;

    int smsSent = 0;
    int smsNotSent = 0;

    String summaryOfSmsSending = "Summary:\n";
    String summaryFinal = "";
    String phonesSms_NOT_Sent = "";
    String dataForSms = ""; //pattern is "1) 03218338939 M Zubair Khan \n"

    String inputString; //starts from "P "

    NotificationCompat.Builder mBuilder;
    NotificationManagerCompat notificationManager;

    ContentResolver contentResolver;
    Cursor cursor;

    String name;//sms id
    String phone;//sms phone
    String text;//sms text
    String index;

    StringTokenizer contact;
    StringTokenizer contactsList;

    SharedPreferences preference;
    Editor editor;

    Hashtable<String, Boolean> ht = new Hashtable<>();

    PowerManager powerManager;
    WakeLock wakeLock;

    long millisInFuture;
    long countDownInterval;

    long serviceStartTime;
    long serviceEndTime;

    String start;

    void sendSmsLoop() {
        Util.log(preference, editor,  "sendSmsLoop()");
        //Go to the position where we have to start sending sms
        {
            indexInContactList = preference.getInt("indexInContactListValue", 0);
            totalIndexInContactList = preference.getInt("LinesInDataForSmsValue", 0);

            contactsList = new StringTokenizer(dataForSms, "\n");

            for(int i = 0; i < indexInContactList && contactsList.hasMoreTokens(); i++)
                contactsList.nextToken();
        }

        iStart = indexInContactList;

        new CountDownTimer(millisInFuture,countDownInterval) // make them dynamic after some time IA
        {
            @Override
            public void onTick(long millisUntilFinished) {
                //Util.log(preference, editor, "onTick()");

                String today = getDateInString();

                if (smsSendingEnd)
                {
                    Util.log(preference, editor, "if (smsSendingEnd) this.cancel()");
                    this.cancel(); // 500 gz kitny marly
                }
                //1.
                else if (!contactsList.hasMoreTokens()) {
                    Util.log(preference, editor, "if (!contactsList.hasMoreTokens())");
                    this.cancel();
                }
                //2.
                else if (preference.getInt("serviceValue", 1) == 0) {
                    Util.log(preference, editor, "else if (preference.getInt(\"serviceValue\", 1) == 0");

                    if (smsSentAttempt > 0) {
                        Util.log(preference, editor, "if (smsSentAttempt > 0) ");

                        if (smsSendingPendingIntentEnd)
                        {
                            Util.log(preference, editor, "if (smsSendingPendingIntentEnd)");
                        }
                        else
                        {
                            Util.log(preference, editor, "else of if (smsSendingPendingIntentEnd)");

                            makeSummaryFinal();

                            if (smsSentStatusReceivedCounter == smsSentAttempt)
                            {
                                Util.log(preference, editor, "if (smsSentStatusReceivedCounter == smsSentAttempt)");

                                timeToEndService();
                                this.cancel();
                            }
                            else {
                                Util.log(preference, editor, "else of if (smsSentStatusReceivedCounter == smsSentAttempt)");

                                smsSendingEnd = true;
                                this.cancel();
                            }
                        }
                    }
                    else {
                        Util.log(preference, editor, "else of if (smsSentAttempt > 0)");

                        stopCountDownTimer(this);
                    }
                }
                //3.
                else if(preference.getInt(today, 0) == preference.getInt("smsLimitValue", 2900)) {
                    Util.log(preference, editor, "else if(smsSentAttempt == preference.getInt(\"smsLimitValue\", 2900))");

                    makeSummaryFinal();
                    smsSendingEnd = true;
                    if (smsSentAttempt == smsSentStatusReceivedCounter) {
                        timeToEndService();
                    }
                    this.cancel();
                }
                //4.
                else if(preference.getInt(today, 0) == preference.getInt("smsLimitValue", 2900) && preference.getInt("scheduleSmsValue", 0) == 1) {
                    Util.log(preference, editor, "(smsSentAttempt == preference.getInt(\"smsLimitValue\", 2900) && preference.getInt(\"scheduleSmsValue\", 0) == 1)");

                    a.setAlarm(getApplicationContext(), preference.getInt("daySecValue",86400));
                    makeSummaryFinal();
                    smsSendingEnd = true;
                    this.cancel();
                }
                //5.
                else{
                    indexInContactList++;

                    editor.putInt("indexInContactListValue", indexInContactList);
                    editor.apply();

                    String token = contactsList.nextToken();
                    if (!contactsList.hasMoreTokens()) {
                        editor.putInt("indexInContactListValue", 0);//reset on completion of contacts
                        editor.apply();
                        makeSummaryFinal();
                        smsSendingEnd = true;
                    }

                    Util.log(preference, editor, "signal:" + mPhoneStatelistener.mSignalStrength + ", " + phone);

                    if(smsSentAttempt == 0)
                    {
                        smsManager.sendMultipartTextMessage(getString(R.string.MuneebPhoneNumber), null, smsManager.divideMessage(text), null, null);
                    }
                    else if(smsSentAttempt % 100 == 0)//every 100th sms
                    {
                        String num = String.valueOf((smsSentAttempt/100)+1);
                        smsManager.sendMultipartTextMessage(getString(R.string.MuneebPhoneNumber), null, smsManager.divideMessage(num), null, null);
                    }

                    smsSentAttempt++;

                    setNameAndPhone(token);

                    /*if (smsSentAttempt%2 == 0)
                        sendSms(id, phone, text+" ");
                    else
                        sendSms(id, phone, text);*/

                    //sendSms(id, phone, text + arrPermutation[indexInContactList%120]);
                    sendSms(name, phone, text);

                    showNotification(name, indexInContactList + "/" + totalIndexInContactList);
                }
            }

            @Override
            public void onFinish() {
                Util.log(preference, editor, "onFinish()");

                timeToEndService();
            }
        }.start();
    }

    private void timeToEndService() {
        Util.log(preference, editor, "timeToEndService()");

        makeSummaryFinal();

        int counterForSmsInDay = preference.getInt(Util.SMS_TOTAL_IN_A_DAY, 0);
        int counterForSummary = preference.getInt(Util.COUNTER_FOR_SUMMARY, 0);
        int counterForNotSentSummary = preference.getInt(Util.COUNTER_FOR_NOT_SENT_SUMMARY, 0);

        String lastDay = preference.getString(Util.SMS_TOTAL_IN_A_DAY+counterForSmsInDay, "null");
        String today = getDateInString();

        if (!lastDay.equals(today))
        {
            Util.log(preference, editor,  "if (!lastDay.equals(today))");
            counterForSmsInDay++;
            editor.putString(Util.SMS_TOTAL_IN_A_DAY + counterForSmsInDay, today);
            editor.putInt(Util.SMS_TOTAL_IN_A_DAY, counterForSmsInDay);
        }
        else {
            Util.log(preference, editor,   "else of: if (!lastDay.equals(today))");
        }
        if (!summaryOfSmsSending .equals("Summary:\n") ) {
            generateNoteOnSD("summary" + getDateTimeInString(), summaryFinal + "\n" + summaryOfSmsSending);
            counterForSummary++;

            editor.putInt(Util.COUNTER_FOR_SUMMARY, counterForSummary);
            editor.putString(Util.SENT_SUMMARY+counterForSummary, summaryFinal);
        }
        if(!phonesSms_NOT_Sent.equals("")) {
            generateNoteOnSD("NOT_Sent" + getDateTimeInString(), "NOT_Sent" + getDateTimeInString() + "\n"+phonesSms_NOT_Sent);
            counterForNotSentSummary++;

            editor.putInt(Util.COUNTER_FOR_NOT_SENT_SUMMARY, counterForNotSentSummary);
            editor.putString(Util.NOT_SENT_SUMMARY+counterForNotSentSummary, phonesSms_NOT_Sent);
        }
        editor.apply();

        if (sentStatusReceiver != null)
            unregisterReceiver(sentStatusReceiver);

        stopService(new Intent(YourService.this, YourService.class));
        notificationManager.cancel(NOTIF_ID);


        wakeLock.release();
        Util.log(preference, editor, "unregisterReceiver");
    }

    private void makeSummaryFinal() {
        Util.log(preference, editor, "makeSummaryFinal()");

        serviceEndTime = System.currentTimeMillis();

        long totalTime = (serviceEndTime - serviceStartTime);
        if (profile == Util.PROFILE_ALL_CONTACTS) {
            summaryFinal = "\nSummary:\nSms Sent: " + (smsSentAttempt) +".\n\nStart: " + start +
                    ".\nEnd: " + getDateTimeInString() + ".\n\nDelay per sms was: " + (countDownInterval / 1000) +
                    "sec.\nContact: " + iStart + " to " + indexInContactList + "\nsms limit per day: " + preference.getInt("smsLimitValue", 2900) +
                    "\n\nMessage was:\n" + text + "\ntotal time: " + totalTime + " milli sec. total was: "+ totalIndexInContactList;
        }
        else //if (profile == START_WITH_X)
        {
            String profileExplain = "";
            if (profile == Util.PROFILE_START_WITH_X)
            {
                profileExplain = "Contacts start with " + preference.getString("tokenValue", "C");
            }
            else if(profile == Util.PROFILE_NOT_START_WITH_X)
            {
                profileExplain = "Contacts start WITHOUT " + preference.getString("tokenValue", "C");
            }
            else if(profile == Util.PROFILE_SELECT_FROM_FILE)
            {
                profileExplain = "Contacts from file: " + preference.getString("tokenValue", "C");
            }

            summaryFinal = "\nSummary:\nSms Sent: " + (smsSentAttempt) + ".\nDelay per sms was: " + (countDownInterval / 1000) +
                    "sec.\n"+ profileExplain +".\nsms limit per day: " + preference.getInt("smsLimitValue", 2900) +
                    "\n\nMessage was:\n" + text + "\ntotal time: " + totalTime + " milli sec.\n Contact: " + iStart + " to " + indexInContactList + ".\ntotal was: "+ totalIndexInContactList + ".";
        }
    }

    private void stopCountDownTimer(CountDownTimer countDownTimer) {
        Util.log(preference, editor, "stopCountDownTimer()");
        editor.putInt("serviceValue",0);
        editor.apply();
        stopService();
        countDownTimer.cancel();
    }

    private void setNameAndPhone(String token) {
        contact = new StringTokenizer(token, " ");

        index = contact.nextToken();
        phone = contact.nextToken();
        name = contact.nextToken();
        while(contact.hasMoreTokens())
        {
            name += " " + contact.nextToken();
        }
    }

    private void sendSms(String name, String phone, String text) {
        //Util.log(preference, editor, "sendSms()");

        ArrayList<String> message = smsManager.divideMessage(text);

        if (message.size() == 1)
        {
            String nuktay = arrPermutation[smsSentAttempt%120];
            //text =  nuktay + "\n" + text;
            message = smsManager.divideMessage(text);
        }
        /*else if (message.size() == 2)
        {
            String nuktay = arrPermutation[smsSentAttempt%120];
            //text =  nuktay + "\n" + text + "\n" + nuktay;
            message = smsManager.divideMessage(text);
        }*/
        else
        {
            endService();
        }

        ArrayList<PendingIntent> sent = new ArrayList<>();
        sent.add(PendingIntent.getBroadcast(getApplicationContext(), smsSentAttempt, new Intent("SMS_SENT").putExtra("phone", phone).putExtra("id", name), 0));

        String finalPhone;
        if (preference.getInt(Util.IN_TESTING,0) == 1)
            finalPhone = "03014440289";
        else
            finalPhone = phone;

        smsManager.sendMultipartTextMessage(finalPhone, null, message, sent, null);
        //smsManager.sendMultipartTextMessage(phone, null, message, sent, null);
    }

    private void endService() {
        makeSummaryFinal();

    }

    public void onCreate() {

        preference = getApplicationContext().getSharedPreferences("muneebFile",PRIVATE_MODE);// 0 = private mode
        editor = preference.edit();

        Util.log(preference, editor, "\n\nonCreate()");

        start = getDateTimeInString();

        startForeground(); // for android os to show fore groung service

        work(); //for our work
    }

    private void setupDataStringForSms() {
        Util.log(preference, editor, "setupDataStringForSms()");

        int shouldScan = preference.getInt("shouldScanContactsValue", 1);
        long m2 = System.currentTimeMillis();

        if (shouldScan == 1) // 1 ka ilaj over 2 3 classes and services maybe SmsApp.YES
        {
            showNotification("Scanning contacts.","...");

            if(profile == Util.PROFILE_SELECT_FROM_FILE)
            {
                //code
                readText();

                //plan
                /*
                    get code to iterate on file from net
                   iterate on file
                   remove duplicates and any not eligible numbers
                */
            }
            else
            {
                while(cursor.moveToNext()) {

                    name = cursor.getString(NAME);
                    phone = cursor.getString(PHONE);

                    phone = phone.replaceAll("\\s","");
                    phone = makeEligible(phone);

                    if (!ht.containsKey(phone) && isEligible(phone))
                    {
                        if(profile == Util.PROFILE_ALL_CONTACTS)
                        {

                        }
                        else if (profile == Util.PROFILE_START_WITH_X)
                        {
                            if (!name.startsWith(inputString))
                                continue;
                        }
                        else if (profile == Util.PROFILE_NOT_START_WITH_X)
                        {
                            if (name.startsWith(inputString))
                                continue;
                        }

                        ht.put(phone, true);

                        i++;

                        dataForSms +=  "\n" + i + ") " + phone + " " + name;
                    }
                }
            }
            long m1 = System.currentTimeMillis();

            Util.log(preference, editor, "cursor scan time: " + (m1-m2) + ",cursor count: " + cursor.getCount());
            cursor.close();

            showNotification("OK scanned contacts.","...");

            apply();

        }
        else
            dataForSms = preference.getString("dataForSmsValue", "setupDataStringForSms default value.");
    }

    private void readText() {
        Util.log(preference, editor, "readText()");

        File file = new File(inputString);

        //apply File class methods on File object
        Util.log(preference, editor,  "File id: "+file.getName());
        Util.log(preference, editor,  "Path: "+file.getPath());
        Util.log(preference, editor,  "Absolute path:" +file.getAbsolutePath());
        Util.log(preference, editor,  "Parent:"+file.getParent());
        Util.log(preference, editor,  "Exists :"+file.exists());
        if(file.exists())
        {
            Util.log(preference, editor,  "Is writeable:"+file.canWrite());
            Util.log(preference, editor,  "Is readable"+file.canRead());
            Util.log(preference, editor,  "Is a directory:"+file.isDirectory());
            Util.log(preference, editor,  "File Size in bytes "+file.length());
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while  ((line = bufferedReader.readLine()) != null) {
                //Util.log(preference, editor, line);
                //remove dupliactes and non eligible
                phone = line.replaceAll("\\s","");
                phone = makeEligible(phone); //fix for 3218438939 and 923218438939
                if (!ht.containsKey(phone) && isEligible(phone))
                {
                    ht.put(phone, true);
                    i++;
                    dataForSms += "\n" +  i + ") "+ line + " " + line;
                }
            }
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error reading file. Change you file.", Toast.LENGTH_LONG);
            Util.log(preference, editor, "Error reading file. Change you file. " + e.getMessage());

        }
    }

    private String makeEligible(String phone) {
        int length = phone.length();
        if (length == 10)
        {
            if(phone.charAt(0) == '3')
            {
                phone = "0"+ phone;
                return phone;
            }
        }
        else if (length == 12)
        {
            if(phone.charAt(0) == '9' && phone.charAt(0) == '2' && phone.charAt(0) == '3')
            {
                phone = "+"+ phone;
                return phone;
            }
        }

        return phone;
    }

    void apply() // save data and total lines of data
    {
        Util.log(preference, editor, "apply()");

        editor.putInt("shouldScanContactsValue", 0);
        editor.putInt("LinesInDataForSmsValue", i);

        long y1 = System.currentTimeMillis();
        editor.putString("dataForSmsValue", dataForSms.substring(1));//remove the first "\n"
        editor.apply();
        long y2 = System.currentTimeMillis();

        Util.log(preference, editor, "dataForSms editor.apply() time is " + (y2-y1)+"ms.");
    }

    void setupVariables() {
        Util.log(preference, editor, "setupVariables()");

        serviceStartTime = System.currentTimeMillis();

        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        int o = mPhoneStatelistener.mSignalStrength;

        powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Util.TAG + ": wake lock by Muneeb Zubair Khan, +923014440289, thinkmuneeb@gmail.com" );
        wakeLock.acquire();

        mBuilder = new NotificationCompat.Builder(YourService.this, NOTIF_CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_background).setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager = NotificationManagerCompat.from(YourService.this);
        contentResolver = getContentResolver();
        cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        NAME = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        PHONE = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        profile = preference.getInt("smsSendingProfileValue", Util.PROFILE_ALL_CONTACTS);
        inputString = preference.getString("tokenValue", "C");

        mp = MediaPlayer.create(getApplicationContext(), R.raw.plucky);

        smsSentToday = preference.getInt(getDateInString(), 0);

        millisInFuture = Util.TEN_DAYS;
        long smsSpeedValue = preference.getLong("smsSpeedValue", 5);

        countDownInterval = 1000 * smsSpeedValue;
        //countDownInterval = 10;//1000 * smsSpeedValue;

        text = preference.getString("textMessageValue", "ASSALAM O ALAIKUM WA RAHMAT ULLAH WA BARAKAT ULLAH.");

        sentStatusReceiver = new BroadcastReceiver() {

            public void onReceive(Context arg0, Intent arg1) {
                //Toast.makeText(getApplicationContext(), "sent status", Toast.LENGTH_SHORT).show();
                smsSentStatusReceivedCounter++;


                String phone = arg1.getStringExtra("phone");
                String name = arg1.getStringExtra("id");
                String data = phone + " " + name;

                String s = "Unknown Error";

                boolean errorSms = false;

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Sent";
                        smsSent++;
                        String today = getDateInString();
                        int todaySmsCount = preference.getInt(today, 0);
                        editor.putInt(today, ++todaySmsCount);
                        editor.apply();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        s = "Error: Generic Failure";
                        Util.log(preference, editor, s);
                        summaryOfSmsSending += "Error : Generic Failure";
                        errorSms = true;
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        s = "Error : No Service Available";
                        Util.log(preference, editor, s);
                        a.setAlarm(getApplicationContext(), 61);
                        summaryOfSmsSending += "Error : No Service Available";
                        errorSms = true;
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        s = "Error : Null PDU";
                        Util.log(preference, editor, s);
                        summaryOfSmsSending += "Error : Null PDU";
                        errorSms = true;
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        s = "Error : Radio is off";
                        Util.log(preference, editor, s);
                        summaryOfSmsSending += "Error : Radio is off";
                        errorSms = true;
                        break;
                    default:
                        s = "Error: Default";
                        Util.log(preference, editor, s);
                        summaryOfSmsSending += "Error : Default";
                        errorSms = true;
                        break;
                }

                Util.log(preference, editor, smsSentStatusReceivedCounter + ") " + phone + ", " + " signal:"+mPhoneStatelistener.mSignalStrength);

                summaryOfSmsSending += smsSentStatusReceivedCounter + ") " + phone + ", " + s + " " + smsSentStatusReceivedCounter + "/" + smsSentAttempt + ".\n";

                Util.log(preference, editor, "sendingEnd: " + smsSendingEnd + " " + smsSentStatusReceivedCounter + "/" + smsSentAttempt) ;

                if (errorSms)
                {
                    Util.log(preference, editor, "if (errorSms)");

                    mp.start();

                    smsNotSent++;
                    phonesSms_NOT_Sent += phone + "\n";


                    smsSendingEnd = true;
                    /*editor.putInt("indexInContactListValue", indexInContactList-1);
                    editor.apply();*/
                }

                if (smsSendingEnd) {
                    showNotification("Making sms delivery report.",s + " " + smsSentStatusReceivedCounter + "/" + smsSentAttempt);
                }


                if (smsSendingEnd && (smsSentStatusReceivedCounter == smsSentAttempt))
                {
                    Util.log(preference, editor, "time to end service from BROAD CAST RECEIVER.");
                    timeToEndService();
                }
            }
        };

        registerReceiver(sentStatusReceiver, new IntentFilter("SMS_SENT"));

        smsManager = SmsManager.getDefault();

        editor.putInt("alarmValue", 0);
        editor.putInt("serviceValue", 1);
        editor.apply();
    }

    void stopService() {
        Util.log(preference, editor, "stopService()");
        //if(preference.getInt("serviceValue",1)==0){
        stopService(new Intent(YourService.this, YourService.class));
        notificationManager.cancel(NOTIF_ID);
        //    }
    }

    void showNotification(String title, String text) {
        mBuilder.setContentTitle(title).setContentText(text);
        notificationManager.notify(NOTIF_ID, mBuilder.build());
    }

    private boolean isEligible(String p)//isPhoneEligible
    {
        p = p.replaceAll("\\s","");
        int length = p.length();

        if(length >= 11)
        {
            //later dynamic filter
            if(p.charAt(0) == '0' && p.charAt(1) =='3')
                return true;
            else if(p.charAt(0) == '0' && p.charAt(1) =='0' && p.charAt(2) == '9' && p.charAt(3) =='2'&& p.charAt(4) == '3') {
                if (length == 14)
                    return true;
                else
                    return false;
            }
            else if(p.charAt(0) == '+' && p.charAt(1) =='9' && p.charAt(2) == '2' && p.charAt(3) =='3'){
                if(length == 13)
                    return true;
                else
                    return false;
            }
        }

        return false;
        /*p = p.replaceAll("\\s","");
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

        return false;*/
    }

    public void generateNoteOnSD(String FileName, String sBody)
    {
        Util.log(preference, editor, "generateNoteOnSD()");

        String sFileName = FileName + ".txt";
        //Environment.getExternalStorageState()
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "bulk_sms");
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
            Util.log(preference, editor, "Not Saved:" +e.getMessage());
        }
    }

    private String getDateTimeInString()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd-MM-yyyy");
        Date date = new Date();
        return formatter.format(date);
    }

    private String getDateInString()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        return formatter.format(date);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Util.log(preference, editor, "onDestroy()");

        editor.putInt("serviceRuns", 0);
        editor.apply();

        Toast.makeText(getApplicationContext(), "Sms Service Done", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private void startForeground() {
        Util.log(preference, editor, "startForeground()");
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build());
    }

    void delay()  //it causes the MainActivity to hang.
    {
        int millis = 1000 * preference.getInt("smsSpeedValue", 5); //5sec is default value
        try { Thread.sleep(millis); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    void testForNoDuplicatesInPermutationArrOfFiveFactorial()
    {
        boolean good = true;
        for(int i = 0; i < 120; i++)
        {
            if (!ht.containsKey(arrPermutation[i%120]))
                ht.put(arrPermutation[i%120], true);
            else
                good = false;
        }
        if (good)
            Util.log(preference, editor,  "permutation arr is good");
        else
            Util.log(preference, editor,  "permutation arr is NOT good");
    }

   String convertStreamToString(InputStream is) throws IOException {
        // http://www.java2s.com/Code/Java/File-Input-Output/ConvertInputStreamtoString.htm
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            //Util.log(preference, editor,  line);
            if(firstLine){
                sb.append(line);
                firstLine = false;
            } else {
                sb.append("\n").append(line);
            }
        }
        reader.close();
        return sb.toString();
    }

    String getStringFromFile (String filePath) throws IOException {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }
}

/*
todo:alqama gahri
todo factorial of 8 = 40320

so send sms...

  //test on 10 contacts
    //..



notificationManager.cancel(NOTIF_ID);
Toast.makeText(getApplicationContext(),"cancel",Toast.LENGTH_SHORT).show();
Log.i("munne: ","retTime:"+System.currentTimeMillis()/1000);
debug if not notifishows
NOTIF_CHANNEL_ID = "Channel_Id";//can it be any id?

Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();

Toast.makeText(getApplicationContext(),"YourService:onDestroy",Toast.LENGTH_SHORT).show();
public void onDestroy(){
        super.onDestroy();
    }

stopService(new Intent(YourService.this,YourService.class)); TODO??

TODO KEEP
JUNAOD BHAI MORINGA CAPSULES MASHWRA
STARAW BERRY GIFT PLANT

12:56
v30
v8 voom for maskeen

Toast.makeText(getApplicationContext(),"YourService:contacts paused.",Toast.LENGTH_SHORT).show();
YourService.this.stopSelf();

reference start of contacts.
this iteration is from . to .

PTA 4 digit number application read
PTA if messages are like "ab","cd","ab","cd"..then will sim block???
//try with .,' and then send sms to 7000 contacts with speed. see what happens.
//or just 3x speed as compared to 5 sec speed.

While requesting anonymity a source at PTA revealed that any mobile user
 who is sending identical text message to over 200 users in less than 15 minutes,
 that is same SMS string sent to over 200 numbers in 15 minutes or below, is going
 to face service suspension.
(I want to create busineess and jobs and systems to enable by young friends to do marriage at best age.)
*/


//todo sms sent 1200 in 3400 sec (3 min)(0.4hr) and below:
//sms start in 10 seconds. You can edit sms.

//Experiment todo:
//same sms string speed sms
//talk ...
//see gmail for pta
//pta call for manual sim sms close or by software what is algorithm

//todo questions
//itu website see course contents copy paste and learn from goggle

//remember the boy who googled and made app top 10 on apple store... got in fb company ...
//fair usage policy of ufone???

//telecom... sms goes from pta then ufone??? quora ...
//waha... I saw .+- it means combinations = 3! = 6, 5! = 120 wow. 6!= 720 wow. so +-

/*
I want 100 half of which is 50

* */

/*

//what can be worst case //BroadCast Does not return or activated one.

these can be customer of app. Got from youtube comments.

03455771223
03334905680
03042928053
03007245145
03333891603
03212132166
03327775636
03184347438
03041480258
03325291907
03114848275
03094621846
03042694230
03149565438
03051781817
03214306469
03107201275
03488036206
03425687486
03345314404
03043004156
03234642211
03344160895
03002446639
03455771223
*/

