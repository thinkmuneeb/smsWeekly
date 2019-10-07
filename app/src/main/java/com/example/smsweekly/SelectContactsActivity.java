package com.example.smsweekly;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SelectContactsActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    int currentProfile;
    String currentProfileToken = "";

    int lastProfile;
    String lastProfileToken;

    private int PICKFILE_REQUEST_CODE = 1;

    private String filePath = "Press this button to select file.";

    RadioButton radioButtonSelectFile;

    TextView textViewFilePath;

    Button saveButton;

    SharedPreferences preference;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.select_contacts);
        super.onCreate(savedInstanceState);

        /*set iValue and shouldScanContacts
        Logic:
        1. If app is run first time then scan contacts.
        2. If app runs first time then set cureent profile and last profile = 0; ALL CONTACTS
        3. The upper things will be in the MainActivity OnCreate
        4. When we are in SelectContacts Activity then when we enter contacts activity SAVE the current profile in memory.
        5. When we leave the SelectContacts Activity then put then selected profile in memory
        6. Make a function which takes values of two profiles and returns true if they are equal other wise flase.
        7. If twoProfiles equal then do nothing. If not equal then set iValue to 0 and shouldScan contacts to One.
        */


        //use EditText.setFocusable(false) to disable editing
        //EditText.setFocusableInTouchMode(true) to enable editing;

        textViewFilePath = (TextView) findViewById(R.id.textViewFilePath);

        saveButton = (Button)findViewById(R.id.buttonSave);

        RadioGroup selectContactsRadioGroup = (RadioGroup) findViewById(R.id.selectContactsRadioGroup);

        final RadioButton allContactsRadioButton = (RadioButton) findViewById(R.id.allContactsRadioButton);
        final RadioButton startWithXRadioButton = (RadioButton) findViewById(R.id.startWithXRadioButton);
        final RadioButton notStartWithXRadioButton = (RadioButton) findViewById(R.id.notStartWithXRadioButton);
        final RadioButton contactGroupsRadioButton = (RadioButton) findViewById(R.id.contactGroupsRadioButton);

        radioButtonSelectFile = (RadioButton) findViewById(R.id.radioButtonSelectFile);

        final EditText editTextStartWithX = (EditText) findViewById(R.id.editTextStartWithX);
        final EditText editTextNotStartWithX = (EditText) findViewById(R.id.editTextNotStartWithX);

        SharedPreferences p = getApplicationContext().getSharedPreferences("muneebFile",0);//private mode
        final SharedPreferences.Editor e = p.edit();

        //save the last profile
        lastProfile = p.getInt("smsSendingProfileValue", 0);
        lastProfileToken = p.getString("tokenValue", ""); //Default Value Not Matters as at start all contacts are selected so  there is no token.

        preference = getApplicationContext().getSharedPreferences("muneebFile",Util.PRIVATE_MODE);// 0 = private mode
        editor = preference.edit();

        //load saved view of radio group

        //load data in edit texts

        if(p.getInt("smsSendingProfileValue",0) == 0)
        {
            allContactsRadioButton.setChecked(true);
        }

        else if(p.getInt("smsSendingProfileValue",0) == 1) {
            editTextStartWithX.setText(p.getString("tokenValue", "C"));
            startWithXRadioButton.setChecked(true);
            editTextStartWithX.setFocusableInTouchMode(true);
            editTextStartWithX.setTextColor(Color.BLACK);
            editTextNotStartWithX.setFocusable(false);
            editTextNotStartWithX.setTextColor(Color.GRAY);
        }

        else if(p.getInt("smsSendingProfileValue",0) == 2) {
            editTextNotStartWithX.setText(p.getString("tokenValue", "C"));
            notStartWithXRadioButton.setChecked(true);
            editTextNotStartWithX.setFocusableInTouchMode(true);
            editTextNotStartWithX.setTextColor(Color.BLACK);
            editTextStartWithX.setFocusable(false);
            editTextStartWithX.setTextColor(Color.GRAY);
        }

        else if(p.getInt("smsSendingProfileValue",0) == 3) {
            textViewFilePath.setText(p.getString("tokenValue", "C"));
            radioButtonSelectFile.setChecked(true);
        }

        selectContactsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.allContactsRadioButton:
                        editTextStartWithX.setFocusable(false);
                        editTextNotStartWithX.setFocusable(false);
                        editTextStartWithX.setTextColor(Color.GRAY);
                        editTextNotStartWithX.setTextColor(Color.GRAY);
                        saveButton.setEnabled(true);
                        break;

                    case R.id.startWithXRadioButton:
                        editTextStartWithX.setFocusableInTouchMode(true);
                        editTextStartWithX.setTextColor(Color.BLACK);
                        editTextNotStartWithX.setFocusable(false);
                        editTextNotStartWithX.setTextColor(Color.GRAY);
                        saveButton.setEnabled(true);
                        break;

                    case R.id.notStartWithXRadioButton:
                        editTextNotStartWithX.setFocusableInTouchMode(true);
                        editTextNotStartWithX.setTextColor(Color.BLACK);
                        editTextStartWithX.setFocusable(false);
                        editTextStartWithX.setTextColor(Color.GRAY);
                        saveButton.setEnabled(true);
                        break;
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allContactsRadioButton.isChecked()) {
                    currentProfile = 0;
                }
                else if (startWithXRadioButton.isChecked()) {
                    currentProfile = 1;
                    currentProfileToken = editTextStartWithX.getText().toString();
                }
                else if (notStartWithXRadioButton.isChecked()) {
                    currentProfile = 2;
                    currentProfileToken = editTextNotStartWithX.getText().toString();
                }
                else if (radioButtonSelectFile.isChecked()) {
                    currentProfile = 3;
                    currentProfileToken = textViewFilePath.getText().toString();
                }

                if (profilesAreEqual(lastProfile, lastProfileToken, currentProfile, currentProfileToken)) {
                    // do nothings go as it is.
                }
                else {
                    e.putInt("indexInContactListValue", 0); //start from 0 index
                    e.putInt("shouldScanContactsValue",1); //scan contacts again
                }

                e.putInt("smsSendingProfileValue", currentProfile);
                e.putString("tokenValue", currentProfileToken);
                e.apply();

                finish();
            }
        });

        radioButtonSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setEnabled(false);
                editTextStartWithX.setFocusable(false);
                editTextNotStartWithX.setFocusable(false);
                editTextStartWithX.setTextColor(Color.GRAY);
                editTextNotStartWithX.setTextColor(Color.GRAY);

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/plain");
                startActivityForResult(intent, PICKFILE_REQUEST_CODE);
            }
        });

        contactGroupsRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextNotStartWithX.setFocusableInTouchMode(false);
                editTextNotStartWithX.setTextColor(Color.GRAY);
                editTextStartWithX.setFocusable(false);
                editTextStartWithX.setTextColor(Color.GRAY);
                saveButton.setEnabled(true);


            }
        });
    }

    public static String getRealPathFromUri(Activity activity, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    @SuppressLint("ObsoleteSdkInt")
    public String getPathFromURI(Uri uri){
        String realPath="";
// SDK < API11
        if (Build.VERSION.SDK_INT < 11) {
            String[] proj = { MediaStore.Images.Media.DATA };
            @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
            int column_index = 0;
            String result="";
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                realPath=cursor.getString(column_index);
            }
        }
        // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19){
            String[] proj = { MediaStore.Images.Media.DATA };
            CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            if(cursor != null){
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                realPath = cursor.getString(column_index);
            }
        }
        // SDK > 19 (Android 4.4)
        else{
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            String[] column = { MediaStore.Images.Media.DATA };
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{ id }, null);
            int columnIndex = 0;
            if (cursor != null) {
                columnIndex = cursor.getColumnIndex(column[0]);
                if (cursor.moveToFirst()) {
                    realPath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }
        return realPath;
    }
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                if (true)
                    DatabaseUtils.dumpCursor(cursor);

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }else{
                    Toast.makeText(context, "Could not get file path. Please try again", Toast.LENGTH_SHORT).show();
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    contentUri = MediaStore.Files.getContentUri("external");
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return uri.getPath();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // && requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK
        if (resultData != null) {
            String path = resultData.getData().getPath();

            //Util.log(preference, editor, "path: " + getPath(getApplicationContext(), resultData.getData())); //gives null on sd card file
            Util.log(preference, editor, "path: " + path); //gives null on sd card file

            /*if (path.contains("primary"))
                path = path.replaceFirst("primary", "emulated/0");

            path = path.replaceFirst(":", "/");
            path = path.replaceFirst("document", "storage");*/

            path = getPath(getApplicationContext(), resultData.getData());

            Util.log(preference, editor, "path: " + path);

            filePath = path;//resultData.getDataString();
            textViewFilePath.setText(filePath);
            saveButton.setEnabled(true);

            saveButton.performClick();
        }
        else {
            textViewFilePath.setText("Select a text file of phone numbers or change your option.");
            saveButton.setEnabled(false);
        }
    }

    private boolean profilesAreEqual(int lastProfile, String lastProfileToken, int currentProfile, String currentProfileToken) {

        if (lastProfile == currentProfile)
        {
            if (lastProfile == 0) // there are no tokens in profile 0 (ALL CONTACTS)
                return true;
            else
            {
                if (lastProfileToken.equals(currentProfileToken))
                    return true;
            }
        }

        return false; // this is else of 2 ifs.
    }
}


//flutter
//xamarin
//react native
//ionic
//apache cordova