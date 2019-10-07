package com.example.smsweekly;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver
{
    String message;
    String sender;

    SharedPreferences preference;
    SharedPreferences.Editor editor;

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

    @Override
    public void onReceive(Context context, Intent intent)
    {

        preference = context.getSharedPreferences("muneebFile",Util.PRIVATE_MODE);
        editor = preference.edit();

        setMessageAndSender(intent);

        ContentValues values = new ContentValues();
        values.put("address", sender);
        values.put("body", message);
        context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
    }
}
