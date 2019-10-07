package com.example.smsweekly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Authenticate extends BroadcastReceiver {
    private static final int PRIVATE_MODE = 0;
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";


    String message;
    String sender;

    String expiryDate;

    SharedPreferences preference;
    SharedPreferences.Editor editor;


    @Override
    public void onReceive(Context context, Intent intent) {
        preference = context.getSharedPreferences("muneebFile",PRIVATE_MODE);
        editor = preference.edit();

        setMessageAndSender(intent);

        boolean senderOk = sender.equals("+923014440289") || sender.equals("+923218438939") || sender.equals("+923357262981");

        if (senderOk && message.contains("null"))
        {
            editor.putString("bulkSmsAppExpiryDate", "null");
            editor.apply();

            Util.log(preference, editor,  "onReceive authentication sms received. sms expired.");
        }

        else if (senderOk && message.contains("demo"))
        {
            expiryDate = getNextDaysDateTimeInString(1);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

            Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);

            Intent intentActivate = new Intent(Util.APP_REGISTERED);
            Bundle extras = new Bundle();
            extras.putString("send_data", "test");
            intent.putExtras(extras);
            context.sendBroadcast(intentActivate);
        }

        else if(senderOk && message.contains("PACKAGE_1_MONTH"))
        {
            expiryDate = getNextDaysDateTimeInString(32);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_3_MONTH"))
        {
            expiryDate = getNextDaysDateTimeInString(32*3);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_6_MONTH"))
        {
            expiryDate = getNextDaysDateTimeInString(32*6);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_1_YEAR"))
        {
            expiryDate = getNextDaysDateTimeInString(32*12*1);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_2_YEAR"))
        {
            expiryDate = getNextDaysDateTimeInString(32*12*  2);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_4_YEAR"))
        {
            expiryDate = getNextDaysDateTimeInString(32*12*  4);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_8_YEAR"))
        {
            expiryDate = getNextDaysDateTimeInString(32*12*  8);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_16_YEAR"))
        {
            expiryDate = getNextDaysDateTimeInString(32*12*  16);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_32_YEAR"))
        {
            expiryDate = getNextDaysDateTimeInString(32*12*  32);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_64_YEAR"))
        {
            expiryDate = getNextDaysDateTimeInString(32*12*  64);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_128_YEAR"))
        {
            expiryDate = getNextDaysDateTimeInString(32*12*  128);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_256_YEAR"))
        {
            expiryDate = getNextDaysDateTimeInString(32*12*  256);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_500_YEAR"))
        {
            expiryDate = getNextDaysDateTimeInString(32*12*  500);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        else if(senderOk && message.contains("PACKAGE_1000_YEAR"))
        {
            expiryDate = getNextDaysDateTimeInString(32*12*  1000);
            editor.putString("bulkSmsAppExpiryDate", expiryDate);
            editor.apply();

           Util.log(preference, editor,  "onReceive authentication sms received. expiryDate:" + expiryDate);
        }

        Util.log(preference, editor,  "onReceive authentication sms received. " + message + " " + sender + " " + (senderOk) + " " +  (message.contains("demo")));
    }

    void setMessageAndSender(Intent intent)
    {
        //if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();

            if (bundle != null) {
                // get sms objects
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    message = "";
                }
                // large message might be broken into many
                SmsMessage[] messages = new SmsMessage[pdus.length];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sb.append(messages[i].getMessageBody());
                }
                sender = messages[0].getOriginatingAddress();
                message = sb.toString();
            }
            else
            {
                message = "";
            }
    }

    private String getNextDaysDateTimeInString(int days)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
        Date date = new Date();
        String today = formatter.format(date);

        Calendar todayDateInCalender = Calendar.getInstance();

        try{ todayDateInCalender.setTime(formatter.parse(today)); }

        catch(ParseException e){ e.printStackTrace(); }

        todayDateInCalender.add(Calendar.DAY_OF_MONTH, days);

        String newDate = formatter.format(todayDateInCalender.getTime());

        return newDate;
    }
}
