package com.example.smsweekly;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Alarm extends BroadcastReceiver
{
    MediaPlayer mp;
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    @Override
    public void onReceive(Context context, Intent intent)
    {

       /* mp = MediaPlayer.create(context, R.raw.ring);
        mp.start();
*/
        Log.i(Util.TAG, "onReceive()");

        //see what this does
        //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "muneeb:muneeb");
        //wl.acquire();

        // Put here YOUR code.
        SharedPreferences preferenceService = context.getSharedPreferences("muneebFile",0);//private mode
        SharedPreferences.Editor editor = preferenceService.edit();

        int serviceFlag = preferenceService.getInt("serviceValue",0);

        //Toast.makeText(context, "Sms sending started.", Toast.LENGTH_LONG).show(); // For example
        //LOG NAHI CHALTA
        //if (serviceFlag != 0)
        //{
            editor.putInt("serviceValue",1);
            editor.apply();
            //Log.i(Util.TAG, preferenceService.getInt("serviceValue",0)+"");
            context.getApplicationContext().startService(new Intent(context, YourService.class));
        //}
        //else
        //{
          //  Log.i(Util.TAG,  "Service already running. "+serviceFlag);
        //}

        //off the alarm
        editor.putInt("alarmValue",0);//alarm=1=on
        editor.apply();
        //wl.release();
    }
    private String getDateTimeInString()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
        Date date = new Date();
        return formatter.format(date);
    }
    public void setAlarm(Context context, int sec)
    {

        Log.i(Util.TAG,  "setAlarm()");
        //mp = MediaPlayer.create(context, R.raw.set);
        //mp.start();
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            //long = 9,223,372,036,854,775,807 more than 1 year.
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+(sec*1000), pi); // Millisec * Second * Minute
            //Toast.makeText(context,"SMS sending will start in " + sec + " seconds.",Toast.LENGTH_LONG).show();
            Log.i(Util.TAG, "setTime: "+System.currentTimeMillis()/1000);
        }
        else
        {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+(sec*1000), pi); // Millisec * Second * Minute
            //Toast.makeText(context,"SMS sending will start in " + sec + " seconds.",Toast.LENGTH_LONG).show();
            //Toast.makeText(context,"setAlarm:set:sec="+sec,Toast.LENGTH_SHORT).show();
        }

        SharedPreferences preferenceService = context.getSharedPreferences("muneebFile",0);//private mode
        SharedPreferences.Editor editor = preferenceService.edit();

        int seconds = sec;
        String smsAlarm;
        if (seconds/86400 > 1)
            smsAlarm = ("SMS sending will start in " + (seconds/86400) + " days.\nFrom:"+getDateTimeInString());
        else if(seconds/(60*60) > 1 )
            smsAlarm = ("SMS sending will start in " + (seconds/3600) + " hours.\nFrom:"+getDateTimeInString());
        else if(seconds/(60) > 1 )
            smsAlarm = ("SMS sending will start in " + (seconds/60) + " minutes.\nFrom:"+getDateTimeInString());
        else
            smsAlarm = ("SMS sending will start in " + seconds + " seconds.\nFrom:"+getDateTimeInString());


        editor.putString("alarmInfoValue", smsAlarm);
        editor.putInt("alarmValue",1);//alarm=1=on
        editor.apply();
    }

    public void cancelAlarm(Context context)
    {
        preference = context.getSharedPreferences("muneebFile",Util.PRIVATE_MODE);// 0 = private mode
        editor = preference.edit();

        Log.i(Util.TAG,  "cancelAlarm()");
        //mp = MediaPlayer.create(context, R.raw.cancel);
        //mp.start();
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Toast.makeText(context,"Sms alarm cancelled.",Toast.LENGTH_SHORT).show();

        SharedPreferences preferenceService = context.getSharedPreferences("muneebFile",0);//private mode
        SharedPreferences.Editor editor = preferenceService.edit();
        editor.putInt("alarmValue",0);//alarm=1=on
        editor.apply();
    }
}
