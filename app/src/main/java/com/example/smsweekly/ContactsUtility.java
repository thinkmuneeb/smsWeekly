package com.example.smsweekly;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.*;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;

import java.util.ArrayList;
import java.util.List;


public  class ContactsUtility {
    public static List<ContactGroup> getGroupsNames(Context context) {

        ContentResolver contentResolver;
        Cursor cursor;

        contentResolver =  context.getContentResolver();
        cursor = contentResolver.query(Data.CONTENT_URI, null, null, null, null);

        List<ContactGroup> selectedGroups = new ArrayList<ContactGroup>();

        while (cursor.moveToNext())
        {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME));
            String description = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID));
            selectedGroups.add(new ContactGroup(name, description));
        }

        return selectedGroups;
    }

    public static ArrayList<ContactGroup> getGroups(Context context){
        ArrayList<ContactGroup> ContactGroups = new ArrayList<ContactGroup>();
        Cursor cursor =  context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI,new String[]{ContactsContract.Groups._ID, ContactsContract.Groups.TITLE}, null, null, null);
        cursor.moveToFirst();
        int len = cursor.getCount();
        String[] a = cursor.getColumnNames();
        for(int i = 0; i < len; i++){
            if(cursor.getColumnIndex(ContactsContract.Groups.DELETED) == 1){
                continue;
            }
            ContactGroups.add(new ContactGroup(cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID)), cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE))));
            cursor.moveToNext();
        }
        return ContactGroups;
    }

    public static String getGroupsSummary(Context context)
    {
        List<ContactGroup> list = getGroupsNames(context);
        String summary = "";

        for (ContactGroup contactGroup: list) {
            summary += contactGroup.id + " ";
            summary += contactGroup.description + "\n";
        }
        return summary;
    }

    public static String getGroupsSummaryOnlineVersion(Context context)
    {
        List<ContactGroup> list = getGroups(context);
        String summary = "";
        for (ContactGroup contactGroup: list) {
            summary += contactGroup.id + " ";
            summary += contactGroup.description + "\n";
        }
        return summary;
    }

    public static void testCodeg(Context context) {//Group Names

        //print all fileds od data table
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(Data.CONTENT_URI, null, null, null, null);

        int m = 0;
        String all = "";
        while (cursor.moveToNext())
        {

            String test = "";
            String testWrongs = "";

            for (int i = 0; i < 1000; i++)
            {
                //int i = 21;
                try {
                    String d1 = cursor.getString(i);
                    if (d1 != null)
                        test += i + ": " + d1 + ", ";
                }
                catch (Exception e)
                {
                    testWrongs += i + ", ";
                }
            }

            String a = test;
            String b = testWrongs;
            String c = testWrongs;

            all += test +"\n";

            if (cursor.getPosition() == 100)
                break;
        }

        String allAgain = all;
        String allAgainAndAgain = all;

    }

    public static void testCode(Context context) {

        getGroupsSummaryOnlineVersion(context);
        //print all fileds od data table
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(Phone.CONTENT_URI, null, null, null, null);

        int mimeNameCol = cursor.getColumnIndex(GroupMembership.MIMETYPE);
        int groupIdCol = cursor.getColumnIndex(GroupMembership.GROUP_ROW_ID);
        int groupNameCol = 21;
        int contactIdCol = cursor.getColumnIndex(Data.RAW_CONTACT_ID);

        //int grupIdCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.GRO);
        String getTableColNames = "";

        for (int i = 0; i < 100; i++)
        {
            try {
                String colName = cursor.getColumnName(i);
                getTableColNames += i + ">" + colName + "\n";
            }
            catch (Exception e){}
        }
//plan join for 1 row in o(1) and n in o(n)

        int m = 0;
        String all = "";
        while (cursor.moveToNext())
        {
            String test = "";
            String testWrongs = "";

            //for (int i = 0; i < 100; i++)
            {
                //int i = 21;
                try {



                    String groupId = cursor.getString(81);
                    String groupName = cursor.getString(groupNameCol);
                    String rawId = cursor.getString(contactIdCol);
                    String phone = cursor.getString(56);//+ cursor.getString(contactIdCol);


                    //String d1 = cursor.getString(i);
                    //if (d1.equals(ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE)) compare with .charAt(UniquePlace) == 'g' later

                    String d3 = GroupMembership.CONTENT_ITEM_TYPE;

                    //if (mimeName.equals(d3))
                    {
                        all += cursor.getPosition() + "> G_source_id: " + rawId + ", " + phone +  "\n";

                        ContentResolver contentResolver2 = context.getContentResolver();
                        Cursor cursor2 = contentResolver.query(Data.CONTENT_URI, null, Data._ID + " = " + m , null, null);

                        int NAME = cursor.getColumnIndex(Phone.DISPLAY_NAME);
                        int PHONE = cursor.getColumnIndex(Phone.NUMBER);

                        String n = cursor.getString(NAME);
                        String p = cursor.getString(PHONE);
                        String d = cursor.getString(PHONE);

                    }
                        //test += i + ": " + d1 + ", ";
                }
                catch (Exception e)
                {
                    //testWrongs += i + ", ";
                }
            }

            //Data table 1 lac 20 k rows

            if (cursor.getPosition() == 1000)
                break;
        }

        String allAgain = all;
        String allAgainAndAgain = all;

    }

    public static void fromStackOverflow(Context context)
    {
        //3 days wasted and learnt that some things are already done on google. Have googling skills.
        //stackoverflow niche for this android

        //30 minutes of code reading everyday is a good start. //https://blog.aritraroy.in/30-bite-sized-pro-tips-to-become-a-better-android-developer-b311fd641089

        //long groupId = id;
        long groupId = 1;
        String[] cProjection = { Contacts.DISPLAY_NAME, GroupMembership.CONTACT_ID };

        Cursor groupCursor = context.getContentResolver().query(
                Data.CONTENT_URI,
                cProjection,
                GroupMembership.GROUP_ROW_ID + "= ?" + " AND "
                        + GroupMembership.MIMETYPE + "='"
                        + GroupMembership.CONTENT_ITEM_TYPE + "'",
                new String[] { String.valueOf(groupId) }, null);
        if (groupCursor != null && groupCursor.moveToFirst())
        {
            do
            {

                int nameCoumnIndex = groupCursor.getColumnIndex(Phone.DISPLAY_NAME);

                String name = groupCursor.getString(nameCoumnIndex);

                long contactId = groupCursor.getLong(groupCursor.getColumnIndex(GroupMembership.CONTACT_ID));

                Cursor numberCursor = context.getContentResolver().query(Phone.CONTENT_URI,
                        new String[] { Phone.NUMBER }, Phone.CONTACT_ID + "=" + contactId, null, null);

                if (numberCursor.moveToFirst())
                {
                    int numberColumnIndex = numberCursor.getColumnIndex(Phone.NUMBER);
                    do
                    {
                        String phoneNumber = numberCursor.getString(numberColumnIndex);
                        Util.show("contact " + name + ":" + phoneNumber);
                    } while (numberCursor.moveToNext());
                    numberCursor.close();
                }
            } while (groupCursor.moveToNext());
            groupCursor.close();
        }
    }
}

//in notify only tell 1/10,000. sms end at 9pm 2-Oct-19.
//Stop time //on night
//start time //next morning
//or make it auto
//or like AI: fajar to isha only.

//Common
// see table columns names to retrieve
// run on console the names.

// Way 1
// make polymorphics
// Inntent to send messages with a string automatically detects phone numbers
//Not ok phones
//Ok phones
//Rules of ok phones +92 0301 length advanced

//Way 2
// add if in select contacts activity.
// drop down to select a group or multiple groups
//learn list view from an app and the getting of data from that item.

//intent that tkaes string and sends sms to phones in that string

//delegates for easily measuring a function time taken
// delegates for reusing a pattern in a loop

//int data1 = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME);
//            int data2 = cursor.getColumnIndex(ContactsContract.Data.DATA2);
//            int data3 = cursor.getColumnIndex(ContactsContract.Data.DATA3);
//            int data4 = cursor.getColumnIndex(ContactsContract.Data.DATA4);
//            int data5 = cursor.getColumnIndex(ContactsContract.Data.DATA5);
//            int data6 = cursor.getColumnIndex(ContactsContract.Data.DATA6);
//            int data7 = cursor.getColumnIndex(ContactsContract.Data.DATA7);
//            int data8 = cursor.getColumnIndex(ContactsContract.Data.DATA8);
//            int data9 = cursor.getColumnIndex(ContactsContract.Data.DATA9);
//            int data10 = cursor.getColumnIndex(ContactsContract.Data.DATA10);
//            int data11 = cursor.getColumnIndex(ContactsContract.Data.DATA11);
//            int data12 = cursor.getColumnIndex(ContactsContract.Data.DATA12);
//            int data13 = cursor.getColumnIndex(ContactsContract.Data.DATA13);
//            int data14 = cursor.getColumnIndex(ContactsContract.Data.DATA14);
//            int data15 = cursor.getColumnIndex(ContactsContract.Data.DATA15);
//
//            String d1 = cursor.getString(data2);
//            String d2 = cursor.getString(data2);
//            String d3 = cursor.getString(data3);
//            String d4 = cursor.getString(data4);
//            String d5 = cursor.getString(data5);
//            String d6 = cursor.getString(data6);
//            String d7 = cursor.getString(data7);
//            String d8 = cursor.getString(data8);
//            String d9 = cursor.getString(data9);
//            String d10 = cursor.getString(data10);
//            String d11 = cursor.getString(data11);
//            String d12 = cursor.getString(data12);
//            String d13 = cursor.getString(data13);
//            String d14 = cursor.getString(data14);
//            String d15 = cursor.getString(data15);
//
//            Util.show("1: " + d1 + ", " + "2: " + d2 + ", " + "3: " + d3 + ", " + "4: " + d4 + ", " + "5: " + d5 + ", " + "6: " + d6 + ", " + "7: " + d7 + ", " + "8: " + d8 + ", " + "9: " + d9 + ", " + "10: " + d10 + ", " + "11: " + d11 + ", " + "12: " + d12 + ", " + "13: " + d13 + ", " + "14: " + d14 + ", " + "15: " + d15);

//